package com.easyhair.finanzas.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.easyhair.finanzas.dto.ResumenCategoriaResponse;
import com.easyhair.finanzas.dto.ResumenPeluqueroResponse;
import com.easyhair.finanzas.dto.ResumenResponse;
import com.easyhair.finanzas.entities.Movimiento;
import com.easyhair.finanzas.services.FinanzasService;
import com.easyhair.finanzas.services.Permisos;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/movimientos")
public class MovimientoController {

    private final FinanzasService finanzasService;

    public MovimientoController(FinanzasService finanzasService) {
        this.finanzasService = finanzasService;
    }

    // reservas-service (al atender) e inventario-service (al comprar) llaman a este endpoint
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Movimiento crear(@Valid @RequestBody Movimiento movimiento,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloAdmin(rol);
        return finanzasService.crear(movimiento);
    }

    @GetMapping
    public List<Movimiento> listar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String tipo,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloAdmin(rol);
        return finanzasService.listar(desde, hasta, tipo);
    }

    @GetMapping("/resumen")
    public ResumenResponse resumen(@RequestParam int anio, @RequestParam int mes,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloAdmin(rol);
        return finanzasService.resumen(anio, mes);
    }

    @GetMapping("/resumen/peluqueros")
    public List<ResumenPeluqueroResponse> resumenPorPeluquero(@RequestParam int anio, @RequestParam int mes,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloAdmin(rol);
        return finanzasService.resumenPorPeluquero(anio, mes);
    }

    @GetMapping("/resumen/categorias")
    public List<ResumenCategoriaResponse> resumenPorCategoria(@RequestParam int anio, @RequestParam int mes,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloAdmin(rol);
        return finanzasService.resumenPorCategoria(anio, mes);
    }

    @GetMapping("/{id}")
    public Movimiento obtener(@PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloAdmin(rol);
        return finanzasService.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloAdmin(rol);
        finanzasService.eliminar(id);
    }
}
