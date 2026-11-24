package com.easyhair.reservas.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.easyhair.reservas.dto.CancelarRequest;
import com.easyhair.reservas.dto.CrearReservaRequest;
import com.easyhair.reservas.dto.DisponibilidadResponse;
import com.easyhair.reservas.dto.ReprogramarRequest;
import com.easyhair.reservas.entities.Reserva;
import com.easyhair.reservas.services.ReservaService;
import com.easyhair.reservas.services.Permisos;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping("/disponibilidad")
    public DisponibilidadResponse disponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long peluqueroId) {
        return reservaService.disponibilidad(fecha, peluqueroId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva crear(@Valid @RequestBody CrearReservaRequest request) {
        return reservaService.crear(request);
    }

    @GetMapping("/{id}")
    public Reserva obtenerPorId(@PathVariable Long id) {
        return reservaService.obtenerPorId(id);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Reserva> reservasDeUsuario(@PathVariable Long usuarioId) {
        return reservaService.reservasDeUsuario(usuarioId);
    }

    // la puede cancelar el cliente o el staff; el gateway manda el rol en X-Usuario-Rol
    @PutMapping("/{id}/cancelar")
    public Reserva cancelar(@PathVariable Long id,
            @RequestBody(required = false) CancelarRequest request,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        String motivo = request != null ? request.getMotivo() : null;
        return reservaService.cancelar(id, motivo, rol);
    }

    @PutMapping("/{id}/reprogramar")
    public Reserva reprogramar(@PathVariable Long id, @Valid @RequestBody ReprogramarRequest request) {
        return reservaService.reprogramar(id, request);
    }

    @GetMapping("/agenda")
    public List<Reserva> agenda(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam Long peluqueroId,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloPeluqueria(rol);
        return reservaService.agenda(fecha, peluqueroId);
    }

    @PutMapping("/{id}/atender")
    public Reserva atender(@PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloPeluqueria(rol);
        return reservaService.atender(id);
    }

    @PutMapping("/{id}/no-asistio")
    public Reserva marcarNoAsistio(@PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloPeluqueria(rol);
        return reservaService.marcarNoAsistio(id);
    }

    @GetMapping("/peluquero/{peluqueroId}/atendidas")
    public List<Reserva> atendidasDePeluquero(@PathVariable Long peluqueroId,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloPeluqueria(rol);
        return reservaService.atendidasDePeluquero(peluqueroId);
    }
}