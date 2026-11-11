package com.easyhair.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.easyhair.inventario.entities.Compra;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findAllByOrderByFechaDescIdDesc();
}
