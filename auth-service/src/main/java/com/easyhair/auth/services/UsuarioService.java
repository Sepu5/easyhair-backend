package com.easyhair.auth.services;

import com.easyhair.auth.dto.ActualizarUsuarioRequest;
import com.easyhair.auth.dto.AuthResponse;
import com.easyhair.auth.dto.LoginRequest;
import com.easyhair.auth.dto.RegistroRequest;
import com.easyhair.auth.dto.UsuarioResponse;
import com.easyhair.auth.dto.ActualizarUsuarioRequest;

public interface UsuarioService {

    AuthResponse registrar(RegistroRequest request);

    AuthResponse login(LoginRequest request);

    UsuarioResponse obtenerPorId(Long id);

    String asignarRol(String correo);

    UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request);
}