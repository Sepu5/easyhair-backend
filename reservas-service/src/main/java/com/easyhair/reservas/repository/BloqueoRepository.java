package com.easyhair.reservas.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.easyhair.reservas.entities.Bloqueo;

public interface BloqueoRepository extends JpaRepository<Bloqueo, Long> {

    List<Bloqueo> findByPeluqueroIdAndFecha(Long peluqueroId, LocalDate fecha);
}