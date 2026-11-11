package com.easyhair.inventario;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.easyhair.inventario.entities.Consumo;
import com.easyhair.inventario.entities.Material;
import com.easyhair.inventario.services.InventarioServiceImpl;

public class InventarioServiceTest {

    // estos métodos no usan la base ni otros microservicios
    private final InventarioServiceImpl service = new InventarioServiceImpl(null, null, null, null);

    private Material crearMaterial(int stock, int stockMinimo) {
        Material material = new Material();
        material.setNombre("Tinte castaño");
        material.setUnidad("ml");
        material.setStock(stock);
        material.setStockMinimo(stockMinimo);
        return material;
    }

    @Test
    public void stockBajoElMinimoAvisa() {
        assertTrue(service.tieneStockBajo(crearMaterial(150, 200)));
    }

    @Test
    public void stockIgualAlMinimoAvisa() {
        assertTrue(service.tieneStockBajo(crearMaterial(200, 200)));
    }

    @Test
    public void stockSobreElMinimoNoAvisa() {
        assertFalse(service.tieneStockBajo(crearMaterial(1000, 200)));
    }

    @Test
    public void aprobarDescuentaElStock() {
        Material material = crearMaterial(1000, 200);
        service.descontarStock(material, 60);
        assertEquals(940, material.getStock());
    }

    @Test
    public void noSePuedeDescontarMasDeLoQueHay() {
        Material material = crearMaterial(50, 200);
        assertThrows(ResponseStatusException.class, () -> service.descontarStock(material, 60));
        assertEquals(50, material.getStock());
    }

    @Test
    public void comprarSumaAlStock() {
        Material material = crearMaterial(150, 200);
        service.sumarStock(material, 2000);
        assertEquals(2150, material.getStock());
    }

    @Test
    public void consumoPendienteSePuedeRevisar() {
        Consumo consumo = new Consumo();
        consumo.setEstado("PENDIENTE");
        assertDoesNotThrow(() -> service.validarPendiente(consumo));
    }

    @Test
    public void consumoYaAprobadoNoSePuedeRevisarDeNuevo() {
        Consumo consumo = new Consumo();
        consumo.setEstado("APROBADO");
        assertThrows(ResponseStatusException.class, () -> service.validarPendiente(consumo));
    }
}
