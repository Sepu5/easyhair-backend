package com.easyhair.reservas.services;

import java.time.LocalDate;
import java.util.List;

import com.easyhair.reservas.dto.CrearReservaRequest;
import com.easyhair.reservas.dto.DisponibilidadResponse;
import com.easyhair.reservas.dto.ReprogramarRequest;
import com.easyhair.reservas.entities.Bloqueo;
import com.easyhair.reservas.entities.Reserva;

public interface ReservaService {

    DisponibilidadResponse disponibilidad(LocalDate fecha, Long peluqueroId);

    Reserva crear(CrearReservaRequest request);

    Reserva obtenerPorId(Long id);

    List<Reserva> reservasDeUsuario(Long usuarioId);

    Reserva cancelar(Long id, String motivo, String rol);

    Reserva reprogramar(Long id, ReprogramarRequest request);

    List<Reserva> agenda(LocalDate fecha, Long peluqueroId);

    Reserva atender(Long id);

    Reserva marcarNoAsistio(Long id);

    List<Reserva> atendidasDePeluquero(Long peluqueroId);

    List<Bloqueo> bloqueos(LocalDate fecha, Long peluqueroId);

    Bloqueo crearBloqueo(Bloqueo bloqueo);

    void eliminarBloqueo(Long id);
}