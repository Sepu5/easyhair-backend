package com.easyhair.reservas.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.easyhair.reservas.entities.Bloqueo;
import com.easyhair.reservas.services.ReservaService;

@RestController
@RequestMapping("api/bloqueos")
public class BloqueoController {

    private final ReservaService reservaService;

    public BloqueoController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public List<Bloqueo> listar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam Long peluqueroId) {
        return reservaService.bloqueos(fecha, peluqueroId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Bloqueo crear(@RequestBody Bloqueo bloqueo) {
        return reservaService.crearBloqueo(bloqueo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        reservaService.eliminarBloqueo(id);
    }
}