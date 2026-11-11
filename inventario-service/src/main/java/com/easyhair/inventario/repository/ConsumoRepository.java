package com.easyhair.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.easyhair.inventario.entities.Consumo;

public interface ConsumoRepository extends JpaRepository<Consumo, Long> {

    // "mis registros" del staff, los más nuevos primero
    List<Consumo> findByStaffIdOrderByFechaRegistroDesc(Long staffId);

    // los pendientes que revisa la administradora, por orden de llegada
    List<Consumo> findByEstadoOrderByFechaRegistroAsc(String estado);

    List<Consumo> findAllByOrderByFechaRegistroDesc();
}
