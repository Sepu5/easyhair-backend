package com.easyhair.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.easyhair.inventario.entities.Material;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByActivoTrueOrderByNombreAsc();
}
