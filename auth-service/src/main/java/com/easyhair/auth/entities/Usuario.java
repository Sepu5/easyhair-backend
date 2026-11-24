package com.easyhair.auth.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    // se guarda encriptada
    @Column(nullable = false)
    private String password;

    // CLIENTE, STAFF o ADMIN (el ADMIN no se puede registrar solo, ver docs/supabase/01_rol_admin.sql)
    @Column(nullable = false)
    private String rol;

    public Usuario() {
    }
}