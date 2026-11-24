package com.easyhair.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public class GatewayTest {

    // clave solo para las pruebas (mínimo 32 caracteres)
    private final JwtFiltro filtro = new JwtFiltro("clave-de-prueba-solo-para-los-tests-123456");

    @Test
    public void loginYRegistroSonPublicos() {
        assertTrue(filtro.esPublica("/api/auth/login", HttpMethod.POST));
        assertTrue(filtro.esPublica("/api/auth/registro", HttpMethod.POST));
    }

    @Test
    public void verElCatalogoEsPublico() {
        assertTrue(filtro.esPublica("/api/servicios", HttpMethod.GET));
        assertTrue(filtro.esPublica("/api/peluqueros", HttpMethod.GET));
    }

    @Test
    public void laDocumentacionSwaggerEsPublica() {
        assertTrue(filtro.esPublica("/swagger-ui.html", HttpMethod.GET));
        assertTrue(filtro.esPublica("/docs/reservas/v3/api-docs", HttpMethod.GET));
        // pero la ruta /docs no deja pasar a la API sin token
        assertFalse(filtro.esPublica("/docs/reservas/api/reservas/usuario/1", HttpMethod.GET));
        assertFalse(filtro.esPublica("/docs/reservas/v3/api-docs/../../api/reservas/usuario/1", HttpMethod.GET));
        assertFalse(filtro.esPublica("/api/reservas/usuario/1", HttpMethod.GET));
    }

    @Test
    public void modificarElCatalogoNecesitaToken() {
        assertFalse(filtro.esPublica("/api/servicios", HttpMethod.POST));
        assertFalse(filtro.esPublica("/api/peluqueros/1", HttpMethod.PUT));
    }

    @Test
    public void reservasNecesitanToken() {
        assertFalse(filtro.esPublica("/api/reservas", HttpMethod.POST));
        assertFalse(filtro.esPublica("/api/reservas/usuario/1", HttpMethod.GET));
    }

    @Test
    public void inventarioYFinanzasNecesitanToken() {
        assertFalse(filtro.esPublica("/api/consumos", HttpMethod.POST));
        assertFalse(filtro.esPublica("/api/movimientos", HttpMethod.GET));
    }

    @Test
    public void seBorranLosHeadersDeUsuarioQueMandaElCliente() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Usuario-Id", "1");
        headers.add("X-Usuario-Rol", "ADMIN");
        headers.add("Content-Type", "application/json");

        JwtFiltro.quitarHeadersDeUsuario(headers);

        assertNull(headers.getFirst("X-Usuario-Id"));
        assertNull(headers.getFirst("X-Usuario-Rol"));
        assertEquals("application/json", headers.getFirst("Content-Type"));
    }
}
