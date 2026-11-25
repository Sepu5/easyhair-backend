package com.easyhair.auth.services;

import java.util.List;

import com.easyhair.auth.dto.ActualizarUsuarioRequest;
import com.easyhair.auth.dto.AuthResponse;
import com.easyhair.auth.dto.CrearUsuarioRequest;
import com.easyhair.auth.dto.LoginRequest;
import com.easyhair.auth.dto.RegistroRequest;
import com.easyhair.auth.dto.UsuarioResponse;

public interface UsuarioService {

    AuthResponse registrar(RegistroRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse renovarToken(String headerAuthorization);

    UsuarioResponse obtenerPorId(Long id);

    String asignarRol(String correo);

    UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request);

    // ---------- administración de usuarios (solo ADMIN) ----------

    List<UsuarioResponse> listar();

    UsuarioResponse crearPorAdmin(CrearUsuarioRequest request);

    UsuarioResponse cambiarRol(Long id, String rol, Long idSesion);

    void eliminar(Long id, Long idSesion);

    // ---------- permisos ----------

    void validarAdmin(String rolSesion);

    void validarPropioOAdmin(Long id, Long idSesion, String rolSesion);
}
