package com.easyhair.auth.services;

import java.util.List;

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
import com.easyhair.auth.dto.ActualizarUsuarioRequest;
import com.easyhair.auth.dto.CrearUsuarioRequest;

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

        @Override
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        String correo = request.getCorreo().trim().toLowerCase();

        // si cambia el correo, revisar que no lo tenga otro usuario
        if (!correo.equals(usuario.getCorreo()) && usuarioRepo.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está en uso");
        }

        usuario.setNombre(request.getNombre().trim());
        usuario.setCorreo(correo);

        String password = request.getPassword();
        if (password != null && !password.isBlank()) {
            if (password.length() < 6) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 6 caracteres");
            }
            usuario.setPassword(encoder.encode(password));
        }

        return aResponse(usuarioRepo.save(usuario));
    }

    // los correos @easyhair.cl son del staff.
    // Nunca devuelve ADMIN: así nadie puede registrarse como administrador desde la app
    @Override
    public String asignarRol(String correo) {
        if (correo.endsWith("@easyhair.cl")) {
            return "STAFF";
        }
        return "CLIENTE";
    }

    // ---------- administración de usuarios (solo ADMIN) ----------

    public static final List<String> ROLES = List.of("CLIENTE", "STAFF", "ADMIN");

    @Override
    public List<UsuarioResponse> listar() {
        return usuarioRepo.findAllByOrderByIdAsc().stream().map(this::aResponse).toList();
    }

    @Override
    public UsuarioResponse crearPorAdmin(CrearUsuarioRequest request) {
        String correo = request.getCorreo().trim().toLowerCase();
        String rol = validarRol(request.getRol());

        if (usuarioRepo.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setCorreo(correo);
        usuario.setPassword(encoder.encode(request.getPassword()));
        usuario.setRol(rol);
        return aResponse(usuarioRepo.save(usuario));
    }

    @Override
    public UsuarioResponse cambiarRol(Long id, String rol, Long idSesion) {
        String nuevoRol = validarRol(rol);

        // si la administradora se quita el rol, el sistema podría quedar sin ningún ADMIN
        if (id.equals(idSesion) && !"ADMIN".equals(nuevoRol)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes quitarte tu propio rol de administrador");
        }

        Usuario usuario = usuarioRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        usuario.setRol(nuevoRol);
        return aResponse(usuarioRepo.save(usuario));
    }

    @Override
    public void eliminar(Long id, Long idSesion) {
        if (id.equals(idSesion)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes eliminar tu propia cuenta");
        }
        if (!usuarioRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        usuarioRepo.deleteById(id);
    }

    // ---------- permisos (el gateway manda el id y el rol del token en los headers) ----------

    // devuelve el rol en mayúsculas o lanza 400 si no existe
    public String validarRol(String rol) {
        String limpio = rol == null ? "" : rol.trim().toUpperCase();
        if (!ROLES.contains(limpio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol debe ser CLIENTE, STAFF o ADMIN");
        }
        return limpio;
    }

    @Override
    public void validarAdmin(String rolSesion) {
        if (!"ADMIN".equals(rolSesion)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo la administración puede hacer esto");
        }
    }

    // cada uno puede ver y editar su propia cuenta; el ADMIN, cualquiera
    @Override
    public void validarPropioOAdmin(Long id, Long idSesion, String rolSesion) {
        if (!"ADMIN".equals(rolSesion) && !id.equals(idSesion)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes ver ni editar la cuenta de otra persona");
        }
    }

    private UsuarioResponse aResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getRol());
    }
}