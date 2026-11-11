package com.easyhair.reservas.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReprogramarRequest {

    @NotNull(message = "Debes elegir una fecha")
    private LocalDate fecha;

    @NotBlank(message = "Debes elegir una hora")
    private String hora;
}