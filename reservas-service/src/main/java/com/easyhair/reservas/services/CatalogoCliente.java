package com.easyhair.reservas.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.easyhair.reservas.dto.PeluqueroDto;

@Service
public class CatalogoCliente {

    private final RestClient restClient;

    public CatalogoCliente(@Value("${servicios.catalogo-url}") String url) {
        this.restClient = RestClient.create(url);
    }

    public List<PeluqueroDto> obtenerPeluqueros() {
        PeluqueroDto[] peluqueros = restClient.get()
                .uri("/api/peluqueros")
                .retrieve()
                .body(PeluqueroDto[].class);
        return Arrays.asList(peluqueros);
    }
}