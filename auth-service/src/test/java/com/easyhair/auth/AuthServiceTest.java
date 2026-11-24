package com.easyhair.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.easyhair.auth.dto.RegistroRequest;
import com.easyhair.auth.entities.Usuario;
import com.easyhair.auth.services.JwtService;
import com.easyhair.auth.services.UsuarioServiceImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class AuthServiceTest {

    private final String claveDePrueba = "clave-de-prueba-solo-para-los-tests-123456";

    // asignarRol no usa la base, por eso se le pasa null al repositorio y al jwt
    private final UsuarioServiceImpl service = new UsuarioServiceImpl(null, null);

    private final Validator validador = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void correoEasyhairEsStaff() {
        assertEquals("STAFF", service.asignarRol("valeria@easyhair.cl"));
    }

    @Test
    public void otroCorreoEsCliente() {
        assertEquals("CLIENTE", service.asignarRol("franco@gmail.com"));
    }

    @Test
    public void elRegistroNuncaCreaUnAdmin() {
        assertEquals("CLIENTE", service.asignarRol("admin@gmail.com"));
        assertEquals("STAFF", service.asignarRol("admin@easyhair.cl"));
    }

    @Test
    public void tokenLlevaCorreoIdYRol() {
        JwtService jwtService = new JwtService(claveDePrueba, 3600000);

        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setCorreo("franco@gmail.com");
        usuario.setRol("CLIENTE");

        String token = jwtService.generarToken(usuario);

        SecretKey clave = Keys.hmacShaKeyFor(claveDePrueba.getBytes(StandardCharsets.UTF_8));
        Claims datos = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();

        assertEquals("franco@gmail.com", datos.getSubject());
        assertEquals("CLIENTE", datos.get("rol"));
        assertEquals(5, ((Number) datos.get("id")).intValue());
    }

    @Test
    public void registroValidoNoTieneErrores() {
        RegistroRequest request = crearRegistro("Franco", "franco@gmail.com", "123456");
        assertTrue(validador.validate(request).isEmpty());
    }

    @Test
    public void passwordCortaEsInvalida() {
        RegistroRequest request = crearRegistro("Franco", "franco@gmail.com", "123");
        assertFalse(validador.validate(request).isEmpty());
    }

    @Test
    public void correoMalEscritoEsInvalido() {
        RegistroRequest request = crearRegistro("Franco", "franco-gmail.com", "123456");
        assertFalse(validador.validate(request).isEmpty());
    }

    @Test
    public void nombreVacioEsInvalido() {
        RegistroRequest request = crearRegistro("", "franco@gmail.com", "123456");
        assertFalse(validador.validate(request).isEmpty());
    }

    private RegistroRequest crearRegistro(String nombre, String correo, String password) {
        RegistroRequest request = new RegistroRequest();
        request.setNombre(nombre);
        request.setCorreo(correo);
        request.setPassword(password);
        return request;
    }

    // ---------- administración de usuarios ----------

    @Test
    public void rolValidoSeDevuelveEnMayusculas() {
        assertEquals("ADMIN", service.validarRol("admin"));
        assertEquals("STAFF", service.validarRol(" STAFF "));
    }

    @Test
    public void rolQueNoExisteEsError() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.validarRol("JEFE"));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        assertThrows(ResponseStatusException.class, () -> service.validarRol(null));
    }

    @Test
    public void soloElAdminPuedeAdministrarUsuarios() {
        assertDoesNotThrow(() -> service.validarAdmin("ADMIN"));

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.validarAdmin("STAFF"));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        assertThrows(ResponseStatusException.class, () -> service.validarAdmin("CLIENTE"));
        assertThrows(ResponseStatusException.class, () -> service.validarAdmin(null));
    }

    @Test
    public void cadaUnoVeSuPropiaCuentaYElAdminCualquiera() {
        assertDoesNotThrow(() -> service.validarPropioOAdmin(5L, 5L, "CLIENTE"));
        assertDoesNotThrow(() -> service.validarPropioOAdmin(9L, 1L, "ADMIN"));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.validarPropioOAdmin(9L, 5L, "STAFF"));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
    }

    @Test
    public void elAdminNoPuedeQuitarseSuPropioRol() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.cambiarRol(1L, "STAFF", 1L));
        assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
    }

    @Test
    public void elAdminNoPuedeEliminarseASiMismo() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.eliminar(1L, 1L));
        assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
    }

    // ---------- renovar el token ----------

    @Test
    public void leerTokenDevuelveLosDatosDelUsuario() {
        JwtService jwtService = new JwtService(claveDePrueba, 3600000);
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setCorreo("valeria@easyhair.cl");
        usuario.setRol("STAFF");

        Claims datos = jwtService.leerToken(jwtService.generarToken(usuario));

        assertEquals("STAFF", datos.get("rol"));
        assertEquals(3, ((Number) datos.get("id")).intValue());
    }

    @Test
    public void sinTokenNoSePuedeRenovar() {
        UsuarioServiceImpl conJwt = new UsuarioServiceImpl(null, new JwtService(claveDePrueba, 3600000));

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> conJwt.renovarToken(null));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        assertThrows(ResponseStatusException.class, () -> conJwt.renovarToken("Basic abc"));
    }

    @Test
    public void unTokenVencidoNoSePuedeRenovar() {
        // expiración negativa: el token nace vencido
        JwtService jwtVencido = new JwtService(claveDePrueba, -1000);
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setCorreo("valeria@easyhair.cl");
        usuario.setRol("STAFF");
        String token = jwtVencido.generarToken(usuario);

        UsuarioServiceImpl conJwt = new UsuarioServiceImpl(null, new JwtService(claveDePrueba, 3600000));
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> conJwt.renovarToken("Bearer " + token));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
    }

    @Test
    public void unTokenFirmadoConOtraClaveNoSePuedeRenovar() {
        JwtService otraClave = new JwtService("otra-clave-distinta-que-no-es-la-del-servidor-999", 3600000);
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCorreo("falso@gmail.com");
        usuario.setRol("ADMIN");
        String tokenFalso = otraClave.generarToken(usuario);

        UsuarioServiceImpl conJwt = new UsuarioServiceImpl(null, new JwtService(claveDePrueba, 3600000));
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> conJwt.renovarToken("Bearer " + tokenFalso));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
    }
}
