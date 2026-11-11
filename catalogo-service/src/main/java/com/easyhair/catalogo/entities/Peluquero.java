package com.easyhair.catalogo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "peluqueros")
public class Peluquero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    // es el mismo correo con el que el peluquero inicia sesión como STAFF
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    @Column(nullable = false, unique = true)
    private String correo;

    private Boolean activo = true;

    public Peluquero() {
    }
}
