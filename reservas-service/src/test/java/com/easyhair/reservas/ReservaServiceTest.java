package com.easyhair.reservas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.easyhair.reservas.entities.Reserva;
import com.easyhair.reservas.services.ReservaServiceImpl;

public class ReservaServiceTest {

    // estos métodos no usan la base ni otros microservicios
    private final ReservaServiceImpl service = new ReservaServiceImpl(null, null, null, null);

    @Test
    public void sinHorasOcupadasEstanTodasLibres() {
        List<String> libres = service.calcularHorasLibres(List.of());
        assertEquals(9, libres.size());
    }

    @Test
    public void lasHorasOcupadasNoAparecenLibres() {
        List<String> libres = service.calcularHorasLibres(List.of("10:00", "13:00"));
        assertEquals(7, libres.size());
        assertFalse(libres.contains("10:00"));
        assertFalse(libres.contains("13:00"));
    }

    @Test
    public void reservaPendienteSePuedeModificar() {
        Reserva reserva = new Reserva();
        reserva.setEstado("PENDIENTE");
        assertDoesNotThrow(() -> service.validarPendiente(reserva));
    }

    @Test
    public void reservaCanceladaNoSePuedeModificar() {
        Reserva reserva = new Reserva();
        reserva.setEstado("CANCELADA");
        assertThrows(ResponseStatusException.class, () -> service.validarPendiente(reserva));
    }

    @Test
    public void reservaAtendidaNoSePuedeModificar() {
        Reserva reserva = new Reserva();
        reserva.setEstado("ATENDIDA");
        assertThrows(ResponseStatusException.class, () -> service.validarPendiente(reserva));
    }

    @Test
    public void horaFueraDelHorarioEsInvalida() {
        LocalDate manana = LocalDate.now().plusDays(1);
        assertThrows(ResponseStatusException.class, () -> service.validarFechaYHora(manana, "20:00"));
    }

    @Test
    public void fechaPasadaEsInvalida() {
        LocalDate ayer = LocalDate.now().minusDays(2);
        assertThrows(ResponseStatusException.class, () -> service.validarFechaYHora(ayer, "10:00"));
    }

    @Test
    public void fechaYHoraValidas() {
        LocalDate manana = LocalDate.now().plusDays(1);
        assertDoesNotThrow(() -> service.validarFechaYHora(manana, "10:00"));
    }

    @Test
    public void staffNoPuedeCancelarSinMotivo() {
        assertThrows(ResponseStatusException.class, () -> service.validarMotivoCancelacion("STAFF", " "));
    }

    @Test
    public void adminNoPuedeCancelarSinMotivo() {
        assertThrows(ResponseStatusException.class, () -> service.validarMotivoCancelacion("ADMIN", ""));
    }

    @Test
    public void staffYAdminSonLaPeluqueria() {
        assertTrue(service.esPeluqueria("STAFF"));
        assertTrue(service.esPeluqueria("ADMIN"));
        assertFalse(service.esPeluqueria("CLIENTE"));
        assertFalse(service.esPeluqueria(null));
    }

    @Test
    public void clientePuedeCancelarSinMotivo() {
        assertDoesNotThrow(() -> service.validarMotivoCancelacion("CLIENTE", null));
        assertDoesNotThrow(() -> service.validarMotivoCancelacion("STAFF", "Me enfermé"));
    }
}
