package com.easyhair.catalogo.services;

import java.util.List;

import com.easyhair.catalogo.entities.Peluquero;
import com.easyhair.catalogo.entities.Servicio;

public interface CatalogoService {

    // servicios
    List<Servicio> listarServicios();

    Servicio obtenerServicio(Long id);

    Servicio crearServicio(Servicio servicio);

    Servicio actualizarServicio(Long id, Servicio datos);

    void desactivarServicio(Long id);

    // peluqueros
    List<Peluquero> listarPeluqueros();

    Peluquero obtenerPeluquero(Long id);

    Peluquero obtenerPeluqueroPorCorreo(String correo);

    Peluquero crearPeluquero(Peluquero peluquero);

    Peluquero actualizarPeluquero(Long id, Peluquero datos);
}
