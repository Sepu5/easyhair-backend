package com.easyhair.reservas.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.easyhair.reservas.entities.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // reservas que ocupan horas ese día (se le pasan los estados PENDIENTE y ATENDIDA)
    List<Reserva> findByPeluqueroIdAndFechaAndEstadoIn(Long peluqueroId, LocalDate fecha, List<String> estados);

    // mis reservas (cliente), las más nuevas primero
    List<Reserva> findByUsuarioIdOrderByFechaDescHoraDesc(Long usuarioId);

    // agenda del día (staff)
    List<Reserva> findByPeluqueroIdAndFechaOrderByHoraAsc(Long peluqueroId, LocalDate fecha);

    // historial de ventas (staff)
    List<Reserva> findByPeluqueroIdAndEstadoOrderByFechaDesc(Long peluqueroId, String estado);
}