package com.easyhair.finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenPeluqueroResponse {

    private Long peluqueroId;
    private Long totalIngresos;
}
