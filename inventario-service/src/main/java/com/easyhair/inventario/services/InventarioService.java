package com.easyhair.inventario.services;

import java.util.List;

import com.easyhair.inventario.dto.CrearCompraRequest;
import com.easyhair.inventario.dto.CrearConsumoRequest;
import com.easyhair.inventario.entities.Compra;
import com.easyhair.inventario.entities.Consumo;
import com.easyhair.inventario.entities.Material;

public interface InventarioService {

    // materiales
    List<Material> listarMateriales();

    Material obtenerMaterial(Long id);

    Material crearMaterial(Material material);

    Material actualizarMaterial(Long id, Material material);

    List<Material> materialesConStockBajo();

    // consumos (los registra el staff)
    Consumo registrarConsumo(CrearConsumoRequest request);

    List<Consumo> consumosDeStaff(Long staffId);

    List<Consumo> listarConsumos(String estado);

    Consumo aprobarConsumo(Long id);

    Consumo rechazarConsumo(Long id);

    // compras (reponer stock)
    Compra registrarCompra(CrearCompraRequest request);

    List<Compra> listarCompras();
}
