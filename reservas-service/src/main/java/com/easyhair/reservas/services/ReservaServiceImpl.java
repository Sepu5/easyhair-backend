package com.easyhair.reservas.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.easyhair.reservas.dto.CrearReservaRequest;
import com.easyhair.reservas.dto.DisponibilidadResponse;
import com.easyhair.reservas.dto.PeluqueroDto;
import com.easyhair.reservas.dto.ReprogramarRequest;
import com.easyhair.reservas.entities.Bloqueo;
import com.easyhair.reservas.entities.Reserva;
import com.easyhair.reservas.repository.BloqueoRepository;
import com.easyhair.reservas.repository.ReservaRepository;

@Service
public class ReservaServiceImpl implements ReservaService {

    // horario del local: la primera hora a las 10:00 y la última a las 18:00
    public static final List<String> HORARIO = List.of(
            "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00");

    // estas reservas ocupan la hora; las CANCELADAS la liberan
    private static final List<String> ESTADOS_ACTIVOS = List.of("PENDIENTE", "ATENDIDA");

    private static final ZoneId ZONA = ZoneId.of("America/Santiago");

    private final ReservaRepository reservaRepo;
    private final BloqueoRepository bloqueoRepo;
    private final CatalogoCliente catalogoCliente;
    private final FinanzasCliente finanzasCliente;

    public ReservaServiceImpl(ReservaRepository reservaRepository, BloqueoRepository bloqueoRepository,
                              CatalogoCliente catalogoCliente, FinanzasCliente finanzasCliente) {
        this.reservaRepo = reservaRepository;
        this.bloqueoRepo = bloqueoRepository;
        this.catalogoCliente = catalogoCliente;
        this.finanzasCliente = finanzasCliente;
    }

    // ---------- disponibilidad ----------

    @Override
    public DisponibilidadResponse disponibilidad(LocalDate fecha, Long peluqueroId) {
        if (peluqueroId != null) {
            List<String> libres = calcularHorasLibres(horasOcupadas(peluqueroId, fecha));
            return new DisponibilidadResponse(fecha, peluqueroId, libres);
        }

        // sin preferencia: horas en que al menos un peluquero está libre
        List<String> libres = new ArrayList<>();
        for (PeluqueroDto peluquero : obtenerPeluqueros()) {
            for (String hora : calcularHorasLibres(horasOcupadas(peluquero.getId(), fecha))) {
                if (!libres.contains(hora)) {
                    libres.add(hora);
                }
            }
        }
        libres.sort(null);
        return new DisponibilidadResponse(fecha, null, libres);
    }

    // ---------- cliente ----------

    @Override
    public Reserva crear(CrearReservaRequest request) {
        validarFechaYHora(request.getFecha(), request.getHora());

        Reserva reserva = new Reserva();
        reserva.setUsuarioId(request.getUsuarioId());
        reserva.setNombreCliente(request.getNombreCliente());
        reserva.setCorreoCliente(request.getCorreoCliente());
        reserva.setServicios(request.getServicios());
        reserva.setTotal(request.getTotal());
        reserva.setFecha(request.getFecha());
        reserva.setHora(request.getHora());

        if (request.getPeluqueroId() == null) {
            PeluqueroDto libre = buscarPeluqueroLibre(request.getFecha(), request.getHora());
            reserva.setPeluqueroId(libre.getId());
            reserva.setNombrePeluquero(libre.getNombre());
        } else {
            if (!estaLibre(request.getPeluqueroId(), request.getFecha(), request.getHora())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Esa hora ya no está disponible");
            }
            reserva.setPeluqueroId(request.getPeluqueroId());
            reserva.setNombrePeluquero(request.getNombrePeluquero());
        }

        reserva.setEstado("PENDIENTE");
        reserva.setFechaCreacion(LocalDateTime.now(ZONA));
        return reservaRepo.save(reserva);
    }

    @Override
    public Reserva obtenerPorId(Long id) {
        return reservaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
    }

    @Override
    public List<Reserva> reservasDeUsuario(Long usuarioId) {
        return reservaRepo.findByUsuarioIdOrderByFechaDescHoraDesc(usuarioId);
    }

    @Override
    public Reserva cancelar(Long id, String motivo, String rol) {
        Reserva reserva = obtenerPorId(id);
        validarPendiente(reserva);
        validarMotivoCancelacion(rol, motivo);

        reserva.setEstado("CANCELADA");
        reserva.setCanceladaPor("STAFF".equals(rol) ? "STAFF" : "CLIENTE");
        if (motivo != null && !motivo.isBlank()) {
            reserva.setMotivoCancelacion(motivo.trim());
        }
        return reservaRepo.save(reserva);
    }

    // si cancela la peluquería, el cliente tiene que saber por qué
    public void validarMotivoCancelacion(String rol, String motivo) {
        if ("STAFF".equals(rol) && (motivo == null || motivo.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica el motivo de la cancelación");
        }
    }

    @Override
    public Reserva reprogramar(Long id, ReprogramarRequest request) {
        Reserva reserva = obtenerPorId(id);
        validarPendiente(reserva);
        validarFechaYHora(request.getFecha(), request.getHora());

        boolean mismoHorario = reserva.getFecha().equals(request.getFecha())
                && reserva.getHora().equals(request.getHora());

        if (!mismoHorario && !estaLibre(reserva.getPeluqueroId(), request.getFecha(), request.getHora())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esa hora ya no está disponible");
        }

        reserva.setFecha(request.getFecha());
        reserva.setHora(request.getHora());
        return reservaRepo.save(reserva);
    }

    // ---------- staff ----------

    @Override
    public List<Reserva> agenda(LocalDate fecha, Long peluqueroId) {
        return reservaRepo.findByPeluqueroIdAndFechaOrderByHoraAsc(peluqueroId, fecha);
    }

    @Override
    public Reserva atender(Long id) {
        Reserva reserva = obtenerPorId(id);
        validarPendiente(reserva);

        // el QR solo sirve el mismo día de la reserva
        if (!reserva.getFecha().equals(LocalDate.now(ZONA))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta reserva es para el " + reserva.getFecha());
        }

        reserva.setEstado("ATENDIDA");
        Reserva guardada = reservaRepo.save(reserva);

        // si finanzas no responde, la reserva igual queda atendida
        try {
            finanzasCliente.registrarIngreso(guardada);
        } catch (Exception e) {
            System.out.println("No se pudo registrar el ingreso en finanzas: " + e.getMessage());
        }
        return guardada;
    }

    @Override
    public Reserva marcarNoAsistio(Long id) {
        Reserva reserva = obtenerPorId(id);
        validarPendiente(reserva);
        reserva.setEstado("NO_ASISTIO");
        return reservaRepo.save(reserva);
    }

    @Override
    public List<Reserva> atendidasDePeluquero(Long peluqueroId) {
        return reservaRepo.findByPeluqueroIdAndEstadoOrderByFechaDesc(peluqueroId, "ATENDIDA");
    }

    // ---------- bloqueos ----------

    @Override
    public List<Bloqueo> bloqueos(LocalDate fecha, Long peluqueroId) {
        return bloqueoRepo.findByPeluqueroIdAndFecha(peluqueroId, fecha);
    }

    @Override
    public Bloqueo crearBloqueo(Bloqueo bloqueo) {
        if (bloqueo.getPeluqueroId() == null || bloqueo.getFecha() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan el peluquero o la fecha");
        }
        validarFechaYHora(bloqueo.getFecha(), bloqueo.getHora());

        for (Reserva reserva : reservaRepo.findByPeluqueroIdAndFechaAndEstadoIn(
                bloqueo.getPeluqueroId(), bloqueo.getFecha(), ESTADOS_ACTIVOS)) {
            if (reserva.getHora().equals(bloqueo.getHora())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya hay una reserva en ese turno");
            }
        }
        for (Bloqueo existente : bloqueoRepo.findByPeluqueroIdAndFecha(bloqueo.getPeluqueroId(), bloqueo.getFecha())) {
            if (existente.getHora().equals(bloqueo.getHora())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese turno ya está bloqueado");
            }
        }

        bloqueo.setId(null);
        return bloqueoRepo.save(bloqueo);
    }

    @Override
    public void eliminarBloqueo(Long id) {
        if (!bloqueoRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bloqueo no encontrado");
        }
        bloqueoRepo.deleteById(id);
    }

    // ---------- lógica que se puede probar sin base de datos ----------

    public List<String> calcularHorasLibres(List<String> ocupadas) {
        List<String> libres = new ArrayList<>();
        for (String hora : HORARIO) {
            if (!ocupadas.contains(hora)) {
                libres.add(hora);
            }
        }
        return libres;
    }

    public void validarPendiente(Reserva reserva) {
        if (!"PENDIENTE".equals(reserva.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden modificar reservas pendientes");
        }
    }

    public void validarFechaYHora(LocalDate fecha, String hora) {
        if (!HORARIO.contains(hora)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora debe ser entre 10:00 y 18:00");
        }
        if (fecha.isBefore(LocalDate.now(ZONA))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede usar una fecha pasada");
        }
    }

    // ---------- ayudantes privados ----------

    private List<String> horasOcupadas(Long peluqueroId, LocalDate fecha) {
        List<String> ocupadas = new ArrayList<>();
        for (Reserva reserva : reservaRepo.findByPeluqueroIdAndFechaAndEstadoIn(peluqueroId, fecha, ESTADOS_ACTIVOS)) {
            ocupadas.add(reserva.getHora());
        }
        for (Bloqueo bloqueo : bloqueoRepo.findByPeluqueroIdAndFecha(peluqueroId, fecha)) {
            ocupadas.add(bloqueo.getHora());
        }
        return ocupadas;
    }

    private boolean estaLibre(Long peluqueroId, LocalDate fecha, String hora) {
        return calcularHorasLibres(horasOcupadas(peluqueroId, fecha)).contains(hora);
    }

    private PeluqueroDto buscarPeluqueroLibre(LocalDate fecha, String hora) {
        for (PeluqueroDto peluquero : obtenerPeluqueros()) {
            if (estaLibre(peluquero.getId(), fecha, hora)) {
                return peluquero;
            }
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "No hay peluqueros disponibles a esa hora");
    }

    private List<PeluqueroDto> obtenerPeluqueros() {
        try {
            return catalogoCliente.obtenerPeluqueros();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo consultar la lista de peluqueros");
        }
    }
}