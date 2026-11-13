package com.easyhair.reservas.dto;

import lombok.Data;

// motivo de la cancelación (obligatorio cuando cancela el staff, opcional para el cliente)
@Data
public class CancelarRequest {

    private String motivo;
}
