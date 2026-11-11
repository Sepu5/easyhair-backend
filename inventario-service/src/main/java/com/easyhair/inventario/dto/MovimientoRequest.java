package com.easyhair.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovimientoRequest {

    private String tipo;
    private Integer monto;
    private String categoria;
    private String descripcion;
    private Long reservaId;
    private Long peluqueroId;
}
