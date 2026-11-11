package com.easyhair.inventario.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.easyhair.inventario.dto.CrearCompraRequest;
import com.easyhair.inventario.dto.CrearConsumoRequest;
import com.easyhair.inventario.entities.Compra;
import com.easyhair.inventario.entities.Consumo;
import com.easyhair.inventario.entities.Material;
import com.easyhair.inventario.repository.CompraRepository;
import com.easyhair.inventario.repository.ConsumoRepository;
import com.easyhair.inventario.repository.MaterialRepository;

@Service
public class InventarioServiceImpl implements InventarioService {

    private static final ZoneId ZONA = ZoneId.of("America/Santiago");

    private final MaterialRepository materialRepo;
    private final ConsumoRepository consumoRepo;
    private final CompraRepository compraRepo;
    private final FinanzasCliente finanzasCliente;

    public InventarioServiceImpl(MaterialRepository materialRepository, ConsumoRepository consumoRepository,
                                 CompraRepository compraRepository, FinanzasCliente finanzasCliente) {
        this.materialRepo = materialRepository;
        this.consumoRepo = consumoRepository;
        this.compraRepo = compraRepository;
        this.finanzasCliente = finanzasCliente;
    }

    // ---------- materiales ----------

    @Override
    public List<Material> listarMateriales() {
        return materialRepo.findByActivoTrueOrderByNombreAsc();
    }

    @Override
    public Material obtenerMaterial(Long id) {
        return materialRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material no encontrado"));
    }

    @Override
    public Material crearMaterial(Material material) {
        material.setId(null);
        if (material.getActivo() == null) {
            material.setActivo(true);
        }
        return materialRepo.save(material);
    }

    @Override
    public Material actualizarMaterial(Long id, Material datos) {
        Material material = obtenerMaterial(id);
        material.setNombre(datos.getNombre());
        material.setUnidad(datos.getUnidad());
        material.setStock(datos.getStock());
        material.setStockMinimo(datos.getStockMinimo());
        material.setPrecioUnitario(datos.getPrecioUnitario());
        if (datos.getActivo() != null) {
            material.setActivo(datos.getActivo());
        }
        return materialRepo.save(material);
    }

    @Override
    public List<Material> materialesConStockBajo() {
        List<Material> bajos = new ArrayList<>();
        for (Material material : listarMateriales()) {
            if (tieneStockBajo(material)) {
                bajos.add(material);
            }
        }
        return bajos;
    }

    // ---------- consumos ----------

    @Override
    public Consumo registrarConsumo(CrearConsumoRequest request) {
        Material material = obtenerMaterial(request.getMaterialId());
        if (!Boolean.TRUE.equals(material.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ese material ya no se usa");
        }

        Consumo consumo = new Consumo();
        consumo.setMaterialId(material.getId());
        consumo.setNombreMaterial(material.getNombre());
        consumo.setCantidad(request.getCantidad());
        consumo.setReservaId(request.getReservaId());
        consumo.setStaffId(request.getStaffId());
        consumo.setNombreStaff(request.getNombreStaff());
        consumo.setComentario(request.getComentario());

        // el stock no cambia hasta que la administradora lo apruebe
        consumo.setEstado("PENDIENTE");
        consumo.setFechaRegistro(LocalDateTime.now(ZONA));
        return consumoRepo.save(consumo);
    }

    @Override
    public List<Consumo> consumosDeStaff(Long staffId) {
        return consumoRepo.findByStaffIdOrderByFechaRegistroDesc(staffId);
    }

    @Override
    public List<Consumo> listarConsumos(String estado) {
        if (estado == null || estado.isBlank()) {
            return consumoRepo.findAllByOrderByFechaRegistroDesc();
        }
        return consumoRepo.findByEstadoOrderByFechaRegistroAsc(estado.toUpperCase());
    }

    @Override
    public Consumo aprobarConsumo(Long id) {
        Consumo consumo = obtenerConsumo(id);
        validarPendiente(consumo);

        Material material = obtenerMaterial(consumo.getMaterialId());
        descontarStock(material, consumo.getCantidad());
        materialRepo.save(material);

        consumo.setEstado("APROBADO");
        consumo.setFechaRevision(LocalDateTime.now(ZONA));
        return consumoRepo.save(consumo);
    }

    @Override
    public Consumo rechazarConsumo(Long id) {
        Consumo consumo = obtenerConsumo(id);
        validarPendiente(consumo);

        consumo.setEstado("RECHAZADO");
        consumo.setFechaRevision(LocalDateTime.now(ZONA));
        return consumoRepo.save(consumo);
    }

    // ---------- compras ----------

    @Override
    public Compra registrarCompra(CrearCompraRequest request) {
        Material material = obtenerMaterial(request.getMaterialId());
        sumarStock(material, request.getCantidad());
        materialRepo.save(material);

        Compra compra = new Compra();
        compra.setMaterialId(material.getId());
        compra.setNombreMaterial(material.getNombre());
        compra.setCantidad(request.getCantidad());
        compra.setCostoTotal(request.getCostoTotal());
        compra.setFecha(LocalDate.now(ZONA));
        Compra guardada = compraRepo.save(compra);

        // si finanzas no responde, la compra igual queda registrada
        try {
            finanzasCliente.registrarEgreso(guardada, material.getUnidad());
        } catch (Exception e) {
            System.out.println("No se pudo registrar el egreso en finanzas: " + e.getMessage());
        }
        return guardada;
    }

    @Override
    public List<Compra> listarCompras() {
        return compraRepo.findAllByOrderByFechaDescIdDesc();
    }

    // ---------- lógica que se puede probar sin base de datos ----------

    public boolean tieneStockBajo(Material material) {
        return material.getStock() <= material.getStockMinimo();
    }

    public void descontarStock(Material material, int cantidad) {
        if (cantidad > material.getStock()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No hay stock suficiente de " + material.getNombre()
                            + " (quedan " + material.getStock() + " " + material.getUnidad() + ")");
        }
        material.setStock(material.getStock() - cantidad);
    }

    public void sumarStock(Material material, int cantidad) {
        material.setStock(material.getStock() + cantidad);
    }

    public void validarPendiente(Consumo consumo) {
        if (!"PENDIENTE".equals(consumo.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este registro ya fue revisado");
        }
    }

    // ---------- ayudantes privados ----------

    private Consumo obtenerConsumo(Long id) {
        return consumoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de consumo no encontrado"));
    }
}
