package com.easyhair.inventario.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.easyhair.inventario.dto.CrearCompraRequest;
import com.easyhair.inventario.entities.Compra;
import com.easyhair.inventario.services.InventarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/compras")
public class CompraController {

    private final InventarioService inventarioService;

    public CompraController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Compra registrar(@Valid @RequestBody CrearCompraRequest request) {
        return inventarioService.registrarCompra(request);
    }

    @GetMapping
    public List<Compra> listar() {
        return inventarioService.listarCompras();
    }
}
