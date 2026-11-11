package com.easyhair.auth.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.easyhair.auth.dto.AuthResponse;
import com.easyhair.auth.dto.LoginRequest;
import com.easyhair.auth.dto.RegistroRequest;
import com.easyhair.auth.dto.UsuarioResponse;
import com.easyhair.auth.entities.Usuario;
import com.easyhair.auth.repository.UsuarioRepository;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.usuarioRepo = usuarioRepository;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse registrar(RegistroRequest request) {
        String correo = request.getCorreo().trim().toLowerCase();

        if (usuarioRepo.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setCorreo(correo);
        usuario.setPassword(encoder.encode(request.getPassword()));
        usuario.setRol(asignarRol(correo));

        Usuario guardado = usuarioRepo.save(usuario);
        return new AuthResponse(aResponse(guardado), jwtService.generarToken(guardado));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String correo = request.getCorreo().trim().toLowerCase();

        Usuario usuario = usuarioRepo.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));

        if (!encoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }

        return new AuthResponse(aResponse(usuario), jwtService.generarToken(usuario));
    }

    @Override
    public UsuarioResponse obtenerPorId(Long id) {
        return usuarioRepo.findById(id)
                .map(this::aResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    // los correos @easyhair.cl son del staff
    @Override
    public String asignarRol(String correo) {
        if (correo.endsWith("@easyhair.cl")) {
            return "STAFF";
        }
        return "CLIENTE";
    }

    private UsuarioResponse aResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getRol());
    }
}