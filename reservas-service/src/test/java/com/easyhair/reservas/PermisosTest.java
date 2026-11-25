package com.easyhair.reservas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.easyhair.reservas.services.Permisos;

public class PermisosTest {

    @Test
    public void soloAdminDejaPasarAlAdmin() {
        assertDoesNotThrow(() -> Permisos.soloAdmin("ADMIN"));
    }

    @Test
    public void soloAdminRechazaAlStaffYAlCliente() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> Permisos.soloAdmin("STAFF"));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        assertThrows(ResponseStatusException.class, () -> Permisos.soloAdmin("CLIENTE"));
    }

    @Test
    public void soloPeluqueriaDejaPasarAlStaffYAlAdmin() {
        assertDoesNotThrow(() -> Permisos.soloPeluqueria("STAFF"));
        assertDoesNotThrow(() -> Permisos.soloPeluqueria("ADMIN"));
    }

    @Test
    public void soloPeluqueriaRechazaAlCliente() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> Permisos.soloPeluqueria("CLIENTE"));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
    }

    @Test
    public void lasLlamadasInternasSinHeaderPasan() {
        // entre microservicios no se pasa por el gateway, así que no viene el rol
        assertDoesNotThrow(() -> Permisos.soloAdmin(null));
        assertDoesNotThrow(() -> Permisos.soloPeluqueria(null));
    }
}
