package com.easyhair.catalogo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.easyhair.catalogo.entities.Peluquero;
import com.easyhair.catalogo.services.CatalogoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/peluqueros")
public class PeluqueroController {

    private final CatalogoService catalogoService;

    public PeluqueroController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    // reservas-service llama a este endpoint para la opción "sin preferencia"
    @GetMapping
    public List<Peluquero> listar() {
        return catalogoService.listarPeluqueros();
    }

    @GetMapping("/{id}")
    public Peluquero obtener(@PathVariable Long id) {
        return catalogoService.obtenerPeluquero(id);
    }

    // la app lo usa para saber qué peluquero es el staff que inició sesión
    @GetMapping("/correo/{correo}")
    public Peluquero obtenerPorCorreo(@PathVariable String correo) {
        return catalogoService.obtenerPeluqueroPorCorreo(correo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Peluquero crear(@Valid @RequestBody Peluquero peluquero) {
        return catalogoService.crearPeluquero(peluquero);
    }

    @PutMapping("/{id}")
    public Peluquero actualizar(@PathVariable Long id, @Valid @RequestBody Peluquero peluquero) {
        return catalogoService.actualizarPeluquero(id, peluquero);
    }
}
