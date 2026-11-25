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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.easyhair.inventario.dto.CrearConsumoRequest;
import com.easyhair.inventario.entities.Consumo;
import com.easyhair.inventario.services.InventarioService;
import com.easyhair.inventario.services.Permisos;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/consumos")
public class ConsumoController {

    private final InventarioService inventarioService;

    public ConsumoController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Consumo registrar(@Valid @RequestBody CrearConsumoRequest request,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloPeluqueria(rol);
        return inventarioService.registrarConsumo(request);
    }

    @GetMapping("/staff/{staffId}")
    public List<Consumo> deStaff(@PathVariable Long staffId,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloPeluqueria(rol);
        return inventarioService.consumosDeStaff(staffId);
    }

    // ?estado=PENDIENTE para los que faltan por revisar
    @GetMapping
    public List<Consumo> listar(@RequestParam(required = false) String estado,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloAdmin(rol);
        return inventarioService.listarConsumos(estado);
    }

    @PutMapping("/{id}/aprobar")
    public Consumo aprobar(@PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloAdmin(rol);
        return inventarioService.aprobarConsumo(id);
    }

    @PutMapping("/{id}/rechazar")
    public Consumo rechazar(@PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        Permisos.soloAdmin(rol);
        return inventarioService.rechazarConsumo(id);
    }
}
