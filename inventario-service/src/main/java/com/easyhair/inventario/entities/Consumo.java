package com.easyhair.inventario.entities;

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
@Table(name = "consumos")
public class Consumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long materialId;
    private String nombreMaterial;

    @Column(nullable = false)
    private Integer cantidad;

    // reserva en la que se usó (puede ir vacía)
    private Long reservaId;

    @Column(nullable = false)
    private Long staffId;
    private String nombreStaff;

    private String comentario;

    // PENDIENTE, APROBADO o RECHAZADO
    @Column(nullable = false)
    private String estado;

    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaRevision;

    public Consumo() {
    }
}
