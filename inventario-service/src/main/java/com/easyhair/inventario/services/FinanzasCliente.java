package com.easyhair.inventario.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.easyhair.inventario.dto.MovimientoRequest;
import com.easyhair.inventario.entities.Compra;

@Service
public class FinanzasCliente {

    private final RestClient restClient;

    public FinanzasCliente(@Value("${servicios.finanzas-url}") String url) {
        this.restClient = RestClient.create(url);
    }

    // cada compra de material es un egreso para finanzas
    public void registrarEgreso(Compra compra, String unidad) {
        MovimientoRequest movimiento = new MovimientoRequest(
                "EGRESO",
                compra.getCostoTotal(),
                "MATERIAL",
                "Compra de " + compra.getNombreMaterial() + " (" + compra.getCantidad() + " " + unidad + ")",
                null,
                null);

        restClient.post()
                .uri("/api/movimientos")
                .body(movimiento)
                .retrieve()
                .toBodilessEntity();
    }
}
