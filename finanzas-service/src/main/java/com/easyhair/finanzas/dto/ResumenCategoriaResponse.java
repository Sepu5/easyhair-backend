package com.easyhair.finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenCategoriaResponse {

    private String categoria;
    private String tipo;
    private Long total;
}
