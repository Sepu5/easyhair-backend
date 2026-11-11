package com.easyhair.finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenResponse {

    private Integer anio;
    private Integer mes;
    private Long totalIngresos;
    private Long totalEgresos;
    private Long ganancia;
}
