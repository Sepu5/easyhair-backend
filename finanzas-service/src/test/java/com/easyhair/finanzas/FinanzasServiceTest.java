package com.easyhair.finanzas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.easyhair.finanzas.dto.ResumenCategoriaResponse;
import com.easyhair.finanzas.dto.ResumenPeluqueroResponse;
import com.easyhair.finanzas.dto.ResumenResponse;
import com.easyhair.finanzas.entities.Movimiento;
import com.easyhair.finanzas.services.FinanzasServiceImpl;

public class FinanzasServiceTest {

    // estos métodos no usan la base de datos
    private final FinanzasServiceImpl service = new FinanzasServiceImpl(null);

    private Movimiento movimiento(String tipo, int monto, String categoria, Long peluqueroId) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipo(tipo);
        movimiento.setMonto(monto);
        movimiento.setCategoria(categoria);
        movimiento.setPeluqueroId(peluqueroId);
        return movimiento;
    }

    @Test
    public void movimientoValidoNoLanzaError() {
        assertDoesNotThrow(() -> service.validarMovimiento(movimiento("INGRESO", 10000, "SERVICIO", 1L)));
    }

    @Test
    public void montoCeroEsInvalido() {
        assertThrows(ResponseStatusException.class,
                () -> service.validarMovimiento(movimiento("INGRESO", 0, "SERVICIO", 1L)));
    }

    @Test
    public void tipoDesconocidoEsInvalido() {
        assertThrows(ResponseStatusException.class,
                () -> service.validarMovimiento(movimiento("GASTO", 5000, "OTRO", null)));
    }

    @Test
    public void categoriaDesconocidaEsInvalida() {
        assertThrows(ResponseStatusException.class,
                () -> service.validarMovimiento(movimiento("EGRESO", 5000, "VIAJES", null)));
    }

    @Test
    public void mesFueraDeRangoEsInvalido() {
        assertThrows(ResponseStatusException.class, () -> service.validarPeriodo(2026, 13));
        assertThrows(ResponseStatusException.class, () -> service.validarPeriodo(2026, 0));
        assertDoesNotThrow(() -> service.validarPeriodo(2026, 11));
    }

    @Test
    public void filtraMovimientosPorTipo() {
        List<Movimiento> todos = List.of(
                movimiento("INGRESO", 10000, "SERVICIO", 1L),
                movimiento("EGRESO", 30000, "MATERIAL", null),
                movimiento("INGRESO", 7000, "SERVICIO", 1L));

        assertEquals(2, service.filtrarPorTipo(todos, "ingreso").size());
        assertEquals(1, service.filtrarPorTipo(todos, "EGRESO").size());
        assertEquals(3, service.filtrarPorTipo(todos, null).size());
    }

    @Test
    public void resumenCalculaLaGanancia() {
        List<Movimiento> movimientos = List.of(
                movimiento("INGRESO", 85000, "SERVICIO", 1L),
                movimiento("EGRESO", 30000, "MATERIAL", null),
                movimiento("EGRESO", 20000, "ARRIENDO", null));

        ResumenResponse resumen = service.calcularResumen(2026, 11, movimientos);

        assertEquals(85000L, resumen.getTotalIngresos());
        assertEquals(50000L, resumen.getTotalEgresos());
        assertEquals(35000L, resumen.getGanancia());
    }

    @Test
    public void resumenPorPeluqueroSumaSoloIngresos() {
        List<Movimiento> movimientos = List.of(
                movimiento("INGRESO", 10000, "SERVICIO", 1L),
                movimiento("INGRESO", 7000, "SERVICIO", 1L),
                movimiento("INGRESO", 5000, "SERVICIO", 2L),
                movimiento("EGRESO", 30000, "MATERIAL", null));

        List<ResumenPeluqueroResponse> resumen = service.calcularResumenPorPeluquero(movimientos);

        assertEquals(2, resumen.size());
        assertEquals(1L, resumen.get(0).getPeluqueroId());
        assertEquals(17000L, resumen.get(0).getTotalIngresos());
        assertEquals(5000L, resumen.get(1).getTotalIngresos());
    }

    @Test
    public void resumenPorCategoriaAgrupaPorTipoYCategoria() {
        List<Movimiento> movimientos = List.of(
                movimiento("INGRESO", 10000, "SERVICIO", 1L),
                movimiento("INGRESO", 5000, "SERVICIO", 2L),
                movimiento("EGRESO", 30000, "MATERIAL", null));

        List<ResumenCategoriaResponse> resumen = service.calcularResumenPorCategoria(movimientos);

        assertEquals(2, resumen.size());
        // EGRESO va antes que INGRESO porque se ordena por tipo
        assertEquals("EGRESO", resumen.get(0).getTipo());
        assertEquals(30000L, resumen.get(0).getTotal());
        assertEquals("SERVICIO", resumen.get(1).getCategoria());
        assertEquals(15000L, resumen.get(1).getTotal());
        assertTrue(resumen.get(1).getTipo().equals("INGRESO"));
    }
}
