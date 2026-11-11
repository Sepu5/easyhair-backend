package com.easyhair.catalogo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.easyhair.catalogo.entities.Servicio;
import com.easyhair.catalogo.services.CatalogoServiceImpl;

public class CatalogoServiceTest {

    // estos métodos no usan la base de datos
    private final CatalogoServiceImpl service = new CatalogoServiceImpl(null, null);

    private Servicio servicio(String nombre, Integer precio, Integer duracion) {
        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setPrecio(precio);
        servicio.setDuracion(duracion);
        return servicio;
    }

    @Test
    public void servicioValidoNoLanzaError() {
        assertDoesNotThrow(() -> service.validarServicio(servicio("Corte Dama", 7000, 45)));
    }

    @Test
    public void servicioSinNombreEsInvalido() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.validarServicio(servicio("  ", 7000, 45)));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    public void precioCeroEsInvalido() {
        assertThrows(ResponseStatusException.class,
                () -> service.validarServicio(servicio("Corte Dama", 0, 45)));
    }

    @Test
    public void precioNegativoEsInvalido() {
        assertThrows(ResponseStatusException.class,
                () -> service.validarServicio(servicio("Corte Dama", -500, 45)));
    }

    @Test
    public void duracionCeroEsInvalida() {
        assertThrows(ResponseStatusException.class,
                () -> service.validarServicio(servicio("Corte Dama", 7000, 0)));
    }

    @Test
    public void elCorreoSeGuardaEnMinusculasYSinEspacios() {
        assertEquals("valeria@easyhair.cl", service.normalizarCorreo("  Valeria@EasyHair.cl "));
    }

    @Test
    public void correoVacioEsInvalido() {
        assertThrows(ResponseStatusException.class, () -> service.normalizarCorreo(" "));
    }
}
