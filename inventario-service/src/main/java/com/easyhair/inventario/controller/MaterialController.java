package com.easyhair.inventario.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.easyhair.inventario.entities.Material;
import com.easyhair.inventario.services.InventarioService;
import com.easyhair.inventario.services.Permisos;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/materiales")
public class MaterialController {

    private final InventarioService inventarioService;

    public MaterialController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public List<Material> listar() {
        return inventarioService.listarMateriales();
    }

    @GetMapping("/stock-bajo")
    public List<Material> stockBajo(@RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloAdmin(rol);
        return inventarioService.materialesConStockBajo();
    }

    @GetMapping("/{id}")
    public Material obtener(@PathVariable Long id) {
        return inventarioService.obtenerMaterial(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Material crear(@Valid @RequestBody Material material,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloAdmin(rol);
        return inventarioService.crearMaterial(material);
    }

    @PutMapping("/{id}")
    public Material actualizar(@PathVariable Long id, @Valid @RequestBody Material material,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloAdmin(rol);
        return inventarioService.actualizarMaterial(id, material);
    }
}
