package com.easyhair.reservas.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CrearReservaRequest {

    @NotNull(message = "Falta el usuario")
    private Long usuarioId;
    private String nombreCliente;
    private String correoCliente;

    // null = sin preferencia
    private Long peluqueroId;
    private String nombrePeluquero;

    @NotBlank(message = "Debes elegir al menos un servicio")
    private String servicios;

    @NotNull(message = "Falta el total")
    @Positive(message = "El total debe ser mayor a 0")
    private Integer total;

    @NotNull(message = "Debes elegir una fecha")
    private LocalDate fecha;

    @NotBlank(message = "Debes elegir una hora")
    private String hora;
}