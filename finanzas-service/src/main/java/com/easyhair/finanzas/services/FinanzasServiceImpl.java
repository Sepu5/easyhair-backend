package com.easyhair.finanzas.services;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.easyhair.finanzas.dto.ResumenCategoriaResponse;
import com.easyhair.finanzas.dto.ResumenPeluqueroResponse;
import com.easyhair.finanzas.dto.ResumenResponse;
import com.easyhair.finanzas.entities.Movimiento;
import com.easyhair.finanzas.repository.MovimientoRepository;

@Service
public class FinanzasServiceImpl implements FinanzasService {

    public static final String INGRESO = "INGRESO";
    public static final String EGRESO = "EGRESO";

    public static final List<String> TIPOS = List.of(INGRESO, EGRESO);
    public static final List<String> CATEGORIAS = List.of(
            "SERVICIO", "MATERIAL", "ARRIENDO", "SERVICIOS_BASICOS", "SUELDOS", "OTRO");

    private static final ZoneId ZONA = ZoneId.of("America/Santiago");

    private final MovimientoRepository movimientoRepo;

    public FinanzasServiceImpl(MovimientoRepository movimientoRepository) {
        this.movimientoRepo = movimientoRepository;
    }

    // ---------- movimientos ----------

    @Override
    public Movimiento crear(Movimiento movimiento) {
        validarMovimiento(movimiento);
        movimiento.setId(null);
        movimiento.setTipo(movimiento.getTipo().trim().toUpperCase());
        movimiento.setCategoria(movimiento.getCategoria().trim().toUpperCase());
        if (movimiento.getFecha() == null) {
            movimiento.setFecha(LocalDate.now(ZONA));
        }
        return movimientoRepo.save(movimiento);
    }

    @Override
    public List<Movimiento> listar(LocalDate desde, LocalDate hasta, String tipo) {
        LocalDate inicio = desde != null ? desde : LocalDate.of(1900, 1, 1);
        LocalDate fin = hasta != null ? hasta : LocalDate.of(9999, 12, 31);
        if (inicio.isAfter(fin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        List<Movimiento> movimientos = movimientoRepo.findByFechaBetweenOrderByFechaDescIdDesc(inicio, fin);
        return filtrarPorTipo(movimientos, tipo);
    }

    @Override
    public Movimiento obtenerPorId(Long id) {
        return movimientoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimiento no encontrado"));
    }

    @Override
    public void eliminar(Long id) {
        Movimiento movimiento = obtenerPorId(id);
        movimientoRepo.delete(movimiento);
    }

    // ---------- resúmenes ----------

    @Override
    public ResumenResponse resumen(int anio, int mes) {
        return calcularResumen(anio, mes, movimientosDelMes(anio, mes));
    }

    @Override
    public List<ResumenPeluqueroResponse> resumenPorPeluquero(int anio, int mes) {
        return calcularResumenPorPeluquero(movimientosDelMes(anio, mes));
    }

    @Override
    public List<ResumenCategoriaResponse> resumenPorCategoria(int anio, int mes) {
        return calcularResumenPorCategoria(movimientosDelMes(anio, mes));
    }

    // ---------- lógica que se puede probar sin base de datos ----------

    public void validarMovimiento(Movimiento movimiento) {
        if (movimiento.getMonto() == null || movimiento.getMonto() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto debe ser mayor a 0");
        }
        if (movimiento.getTipo() == null || !TIPOS.contains(movimiento.getTipo().trim().toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo debe ser INGRESO o EGRESO");
        }
        if (movimiento.getCategoria() == null
                || !CATEGORIAS.contains(movimiento.getCategoria().trim().toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La categoría debe ser una de: " + String.join(", ", CATEGORIAS));
        }
    }

    public void validarPeriodo(int anio, int mes) {
        if (mes < 1 || mes > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mes debe estar entre 1 y 12");
        }
        if (anio < 2000 || anio > 2100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El año no es válido");
        }
    }

    public List<Movimiento> filtrarPorTipo(List<Movimiento> movimientos, String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return movimientos;
        }
        String buscado = tipo.trim().toUpperCase();
        if (!TIPOS.contains(buscado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo debe ser INGRESO o EGRESO");
        }
        List<Movimiento> filtrados = new ArrayList<>();
        for (Movimiento movimiento : movimientos) {
            if (buscado.equals(movimiento.getTipo())) {
                filtrados.add(movimiento);
            }
        }
        return filtrados;
    }

    public ResumenResponse calcularResumen(int anio, int mes, List<Movimiento> movimientos) {
        long ingresos = 0;
        long egresos = 0;
        for (Movimiento movimiento : movimientos) {
            if (INGRESO.equals(movimiento.getTipo())) {
                ingresos += movimiento.getMonto();
            } else if (EGRESO.equals(movimiento.getTipo())) {
                egresos += movimiento.getMonto();
            }
        }
        return new ResumenResponse(anio, mes, ingresos, egresos, ingresos - egresos);
    }

    // solo cuentan los ingresos que tienen peluquero (los de servicios)
    public List<ResumenPeluqueroResponse> calcularResumenPorPeluquero(List<Movimiento> movimientos) {
        Map<Long, Long> totales = new TreeMap<>();
        for (Movimiento movimiento : movimientos) {
            if (INGRESO.equals(movimiento.getTipo()) && movimiento.getPeluqueroId() != null) {
                totales.merge(movimiento.getPeluqueroId(), movimiento.getMonto().longValue(), Long::sum);
            }
        }
        List<ResumenPeluqueroResponse> resultado = new ArrayList<>();
        for (Map.Entry<Long, Long> entrada : totales.entrySet()) {
            resultado.add(new ResumenPeluqueroResponse(entrada.getKey(), entrada.getValue()));
        }
        return resultado;
    }

    public List<ResumenCategoriaResponse> calcularResumenPorCategoria(List<Movimiento> movimientos) {
        Map<String, ResumenCategoriaResponse> grupos = new LinkedHashMap<>();
        for (Movimiento movimiento : movimientos) {
            String clave = movimiento.getTipo() + "|" + movimiento.getCategoria();
            ResumenCategoriaResponse grupo = grupos.get(clave);
            if (grupo == null) {
                grupo = new ResumenCategoriaResponse(movimiento.getCategoria(), movimiento.getTipo(), 0L);
                grupos.put(clave, grupo);
            }
            grupo.setTotal(grupo.getTotal() + movimiento.getMonto());
        }
        List<ResumenCategoriaResponse> resultado = new ArrayList<>(grupos.values());
        resultado.sort(Comparator.comparing(ResumenCategoriaResponse::getTipo)
                .thenComparing(ResumenCategoriaResponse::getCategoria));
        return resultado;
    }

    // ---------- ayudantes privados ----------

    private List<Movimiento> movimientosDelMes(int anio, int mes) {
        validarPeriodo(anio, mes);
        YearMonth periodo = YearMonth.of(anio, mes);
        return movimientoRepo.findByFechaBetweenOrderByFechaDescIdDesc(periodo.atDay(1), periodo.atEndOfMonth());
    }
}
