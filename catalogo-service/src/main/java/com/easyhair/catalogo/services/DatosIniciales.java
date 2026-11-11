package com.easyhair.catalogo.services;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.easyhair.catalogo.entities.Peluquero;
import com.easyhair.catalogo.repository.PeluqueroRepository;

// al iniciar, crea a Valeria Soto (id 1) si todavía no hay peluqueros
@Component
public class DatosIniciales implements CommandLineRunner {

    private final PeluqueroRepository peluqueroRepo;

    public DatosIniciales(PeluqueroRepository peluqueroRepository) {
        this.peluqueroRepo = peluqueroRepository;
    }

    @Override
    public void run(String... args) {
        if (peluqueroRepo.count() == 0) {
            Peluquero valeria = new Peluquero();
            valeria.setNombre("Valeria Soto");
            valeria.setCorreo("valeria@easyhair.cl");
            valeria.setActivo(true);
            peluqueroRepo.save(valeria);
        }
    }
}
