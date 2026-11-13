package com.easyhair.catalogo.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.easyhair.catalogo.entities.Peluquero;
import com.easyhair.catalogo.entities.Servicio;
import com.easyhair.catalogo.repository.PeluqueroRepository;
import com.easyhair.catalogo.repository.ServicioRepository;

@Service
public class CatalogoServiceImpl implements CatalogoService {

    private final ServicioRepository servicioRepo;
    private final PeluqueroRepository peluqueroRepo;

    public CatalogoServiceImpl(ServicioRepository servicioRepository, PeluqueroRepository peluqueroRepository) {
        this.servicioRepo = servicioRepository;
        this.peluqueroRepo = peluqueroRepository;
    }

    // ---------- servicios ----------

    @Override
    public List<Servicio> listarServicios() {
        return servicioRepo.findByActivoTrueOrderByNombreAsc();
    }

    @Override
    public Servicio obtenerServicio(Long id) {
        return servicioRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
    }

    @Override
    public Servicio crearServicio(Servicio servicio) {
        validarServicio(servicio);
        servicio.setId(null);
        if (servicio.getActivo() == null) {
            servicio.setActivo(true);
        }
        return servicioRepo.save(servicio);
    }

    @Override
    public Servicio actualizarServicio(Long id, Servicio datos) {
        validarServicio(datos);
        Servicio servicio = obtenerServicio(id);
        servicio.setNombre(datos.getNombre());
        servicio.setDescripcion(datos.getDescripcion());
        servicio.setPrecio(datos.getPrecio());
        servicio.setDuracion(datos.getDuracion());
        if (datos.getActivo() != null) {
            servicio.setActivo(datos.getActivo());
        }
        return servicioRepo.save(servicio);
    }

    // el servicio no se borra: queda con activo = false para no romper reservas antiguas
    @Override
    public void desactivarServicio(Long id) {
        Servicio servicio = obtenerServicio(id);
        servicio.setActivo(false);
        servicioRepo.save(servicio);
    }

    // ---------- peluqueros ----------

    @Override
    public List<Peluquero> listarPeluqueros() {
        return peluqueroRepo.findByActivoTrueOrderByIdAsc();
    }

    @Override
    public Peluquero obtenerPeluquero(Long id) {
        return peluqueroRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Peluquero no encontrado"));
    }

    @Override
    public Peluquero obtenerPeluqueroPorCorreo(String correo) {
        return peluqueroRepo.findByCorreo(normalizarCorreo(correo))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Peluquero no encontrado"));
    }

    @Override
    public Peluquero crearPeluquero(Peluquero peluquero) {
        peluquero.setCorreo(normalizarCorreo(peluquero.getCorreo()));
        if (peluqueroRepo.findByCorreo(peluquero.getCorreo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un peluquero con ese correo");
        }
        peluquero.setId(null);
        if (peluquero.getActivo() == null) {
            peluquero.setActivo(true);
        }
        return peluqueroRepo.save(peluquero);
    }

    @Override
    public Peluquero actualizarPeluquero(Long id, Peluquero datos) {
        Peluquero peluquero = obtenerPeluquero(id);
        String correo = normalizarCorreo(datos.getCorreo());

        // el correo no puede ser el de otro peluquero
        Peluquero otro = peluqueroRepo.findByCorreo(correo).orElse(null);
        if (otro != null && !otro.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un peluquero con ese correo");
        }

        peluquero.setNombre(datos.getNombre());
        peluquero.setCorreo(correo);
        if (datos.getActivo() != null) {
            peluquero.setActivo(datos.getActivo());
        }
        return peluqueroRepo.save(peluquero);
    }

    // ---------- lógica que se puede probar sin base de datos ----------

    // el correo se guarda sin espacios y en minúsculas, igual que en auth-service
    public String normalizarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo es obligatorio");
        }
        return correo.trim().toLowerCase();
    }

    public void validarServicio(Servicio servicio) {
        if (servicio.getNombre() == null || servicio.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (servicio.getPrecio() == null || servicio.getPrecio() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor a 0");
        }
        if (servicio.getDuracion() == null || servicio.getDuracion() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La duración debe ser mayor a 0");
        }
    }
}
