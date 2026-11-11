package com.easyhair.catalogo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.easyhair.catalogo.entities.Peluquero;

public interface PeluqueroRepository extends JpaRepository<Peluquero, Long> {

    List<Peluquero> findByActivoTrueOrderByIdAsc();

    Optional<Peluquero> findByCorreo(String correo);
}
