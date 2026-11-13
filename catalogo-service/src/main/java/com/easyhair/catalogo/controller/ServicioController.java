package com.easyhair.catalogo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.easyhair.catalogo.entities.Servicio;
import com.easyhair.catalogo.services.CatalogoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/servicios")
public class ServicioController {

    private final CatalogoService catalogoService;

    public ServicioController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    public List<Servicio> listar() {
        return catalogoService.listarServicios();
    }

    @GetMapping("/{id}")
    public Servicio obtener(@PathVariable Long id) {
        return catalogoService.obtenerServicio(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Servicio crear(@Valid @RequestBody Servicio servicio) {
        return catalogoService.crearServicio(servicio);
    }

    @PutMapping("/{id}")
    public Servicio actualizar(@PathVariable Long id, @Valid @RequestBody Servicio servicio) {
        return catalogoService.actualizarServicio(id, servicio);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        catalogoService.desactivarServicio(id);
    }
}
