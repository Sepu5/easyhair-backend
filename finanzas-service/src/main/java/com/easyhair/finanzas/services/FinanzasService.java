package com.easyhair.finanzas.services;

import java.time.LocalDate;
import java.util.List;

import com.easyhair.finanzas.dto.ResumenCategoriaResponse;
import com.easyhair.finanzas.dto.ResumenPeluqueroResponse;
import com.easyhair.finanzas.dto.ResumenResponse;
import com.easyhair.finanzas.entities.Movimiento;

public interface FinanzasService {

    Movimiento crear(Movimiento movimiento);

    List<Movimiento> listar(LocalDate desde, LocalDate hasta, String tipo);

    Movimiento obtenerPorId(Long id);

    void eliminar(Long id);

    ResumenResponse resumen(int anio, int mes);

    List<ResumenPeluqueroResponse> resumenPorPeluquero(int anio, int mes);

    List<ResumenCategoriaResponse> resumenPorCategoria(int anio, int mes);
}
