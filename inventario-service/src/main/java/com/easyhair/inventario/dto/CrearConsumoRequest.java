package com.easyhair.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CrearConsumoRequest {

    @NotNull(message = "Debes elegir un material")
    private Long materialId;

    @NotNull(message = "Debes indicar la cantidad")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    private Long reservaId;

    @NotNull(message = "Falta el staff")
    private Long staffId;
    private String nombreStaff;

    private String comentario;
}
