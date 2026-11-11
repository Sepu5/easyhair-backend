package com.easyhair.reservas.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.easyhair.reservas.dto.MovimientoRequest;
import com.easyhair.reservas.entities.Reserva;

@Service
public class FinanzasCliente {

    private final RestClient restClient;

    public FinanzasCliente(@Value("${servicios.finanzas-url}") String url) {
        this.restClient = RestClient.create(url);
    }

    public void registrarIngreso(Reserva reserva) {
        MovimientoRequest movimiento = new MovimientoRequest(
                "INGRESO",
                reserva.getTotal(),
                "SERVICIO",
                "Reserva " + reserva.getId() + " - " + reserva.getServicios(),
                reserva.getId(),
                reserva.getPeluqueroId());

        restClient.post()
                .uri("/api/movimientos")
                .body(movimiento)
                .retrieve()
                .toBodilessEntity();
    }
}