package com.easyhair.reservas.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // datos del cliente (vienen de auth-service, guardados en la app)
    @Column(nullable = false)
    private Long usuarioId;
    private String nombreCliente;
    private String correoCliente;

    @Column(nullable = false)
    private Long peluqueroId;
    private String nombrePeluquero;

    // ej: "Corte Dama, Lavado"
    @Column(nullable = false)
    private String servicios;

    @Column(nullable = false)
    private Integer total;

    @Column(nullable = false)
    private LocalDate fecha;

    // ej: "10:00"
    @Column(nullable = false)
    private String hora;

    // PENDIENTE, ATENDIDA, CANCELADA o NO_ASISTIO
    @Column(nullable = false)
    private String estado;

    // solo si se cancela: quién la canceló (CLIENTE o STAFF) y por qué
    private String canceladaPor;
    private String motivoCancelacion;

    private LocalDateTime fechaCreacion;

    public Reserva() {
    }
}