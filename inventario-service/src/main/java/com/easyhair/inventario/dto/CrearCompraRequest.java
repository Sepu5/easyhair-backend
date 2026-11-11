package com.easyhair.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CrearCompraRequest {

    @NotNull(message = "Debes elegir un material")
    private Long materialId;

    @NotNull(message = "Debes indicar la cantidad")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "Debes indicar el costo")
    @Positive(message = "El costo debe ser mayor a 0")
    private Integer costoTotal;
}
