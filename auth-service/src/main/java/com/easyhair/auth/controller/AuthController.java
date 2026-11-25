package com.easyhair.auth.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.easyhair.auth.dto.ActualizarUsuarioRequest;
import com.easyhair.auth.dto.AuthResponse;
import com.easyhair.auth.dto.CambiarRolRequest;
import com.easyhair.auth.dto.CrearUsuarioRequest;
import com.easyhair.auth.dto.LoginRequest;
import com.easyhair.auth.dto.RegistroRequest;
import com.easyhair.auth.dto.UsuarioResponse;
import com.easyhair.auth.services.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api")
public class AuthController {

    // el gateway valida el token y nos pasa quién es en estos headers
    private static final String HEADER_ID = "X-Usuario-Id";
    private static final String HEADER_ROL = "X-Usuario-Rol";

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/auth/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return usuarioService.registrar(request);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return usuarioService.login(request);
    }

    // renueva el token antes de que venza (la ruta /api/auth/** es pública en el gateway,
    // por eso el token se valida aquí mismo)
    @PostMapping("/auth/refresh")
    public AuthResponse renovar(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return usuarioService.renovarToken(authorization);
    }

    // ---------- perfil: cada uno el suyo (o el ADMIN cualquiera) ----------

    @GetMapping("/usuarios/{id}")
    public UsuarioResponse obtenerPorId(@PathVariable Long id,
            @RequestHeader(value = HEADER_ID, required = false) Long idSesion,
            @RequestHeader(value = HEADER_ROL, required = false) String rolSesion) {
        usuarioService.validarPropioOAdmin(id, idSesion, rolSesion);
        return usuarioService.obtenerPorId(id);
    }

    @PutMapping("/usuarios/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest request,
            @RequestHeader(value = HEADER_ID, required = false) Long idSesion,
            @RequestHeader(value = HEADER_ROL, required = false) String rolSesion) {
        usuarioService.validarPropioOAdmin(id, idSesion, rolSesion);
        return usuarioService.actualizar(id, request);
    }

    // ---------- administración de usuarios (solo ADMIN) ----------

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listar(@RequestHeader(value = HEADER_ROL, required = false) String rolSesion) {
        usuarioService.validarAdmin(rolSesion);
        return usuarioService.listar();
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody CrearUsuarioRequest request,
            @RequestHeader(value = HEADER_ROL, required = false) String rolSesion) {
        usuarioService.validarAdmin(rolSesion);
        return usuarioService.crearPorAdmin(request);
    }

    @PutMapping("/usuarios/{id}/rol")
    public UsuarioResponse cambiarRol(@PathVariable Long id, @Valid @RequestBody CambiarRolRequest request,
            @RequestHeader(value = HEADER_ID, required = false) Long idSesion,
            @RequestHeader(value = HEADER_ROL, required = false) String rolSesion) {
        usuarioService.validarAdmin(rolSesion);
        return usuarioService.cambiarRol(id, request.getRol(), idSesion);
    }

    @DeleteMapping("/usuarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id,
            @RequestHeader(value = HEADER_ID, required = false) Long idSesion,
            @RequestHeader(value = HEADER_ROL, required = false) String rolSesion) {
        usuarioService.validarAdmin(rolSesion);
        usuarioService.eliminar(id, idSesion);
    }
}
