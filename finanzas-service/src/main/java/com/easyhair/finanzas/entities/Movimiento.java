package com.easyhair.finanzas.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // INGRESO o EGRESO
    @NotBlank(message = "El tipo es obligatorio")
    @Column(nullable = false)
    private String tipo;

    @NotNull(message = "El monto es obligatorio")
    @Min(value = 1, message = "El monto debe ser mayor a 0")
    @Column(nullable = false)
    private Integer monto;

    // SERVICIO, MATERIAL, ARRIENDO, SERVICIOS_BASICOS, SUELDOS u OTRO
    @NotBlank(message = "La categoría es obligatoria")
    @Column(nullable = false)
    private String categoria;

    private String descripcion;

    // solo los ingresos por servicio traen reserva y peluquero
    private Long reservaId;
    private Long peluqueroId;

    // si no viene, se usa la fecha de hoy
    @Column(nullable = false)
    private LocalDate fecha;

    public Movimiento() {
    }
}
