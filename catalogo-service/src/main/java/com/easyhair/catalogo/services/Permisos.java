package com.easyhair.catalogo.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Permisos por rol. El gateway valida el token y manda el rol en el header X-Usuario-Rol.
// Si el header no viene (null) es una llamada interna entre microservicios, que no pasa
// por el gateway y no se puede hacer desde internet, por eso se deja pasar.
public final class Permisos {

    private Permisos() {
    }

    // finanzas, compras, aprobar consumos y editar el catálogo
    public static void soloAdmin(String rol) {
        if (rol != null && !"ADMIN".equals(rol)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo la administración puede hacer esto");
        }
    }

    // agenda, atender, bloqueos y consumos: lo que hace la peluquería (staff o administración)
    public static void soloPeluqueria(String rol) {
        if (rol != null && !"STAFF".equals(rol) && !"ADMIN".equals(rol)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta opción es solo para el personal de la peluquería");
        }
    }
}
