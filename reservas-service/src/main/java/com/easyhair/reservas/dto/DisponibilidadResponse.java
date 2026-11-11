package com.easyhair.reservas.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisponibilidadResponse {

    private LocalDate fecha;
    private Long peluqueroId;
    private List<String> horasDisponibles;
}