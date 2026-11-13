package com.easyhair.inventario.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "compras")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long materialId;
    private String nombreMaterial;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Integer costoTotal;

    private LocalDate fecha;

    public Compra() {
    }
}
