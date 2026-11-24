package com.easyhair.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CambiarRolRequest {

    // CLIENTE, STAFF o ADMIN
    @NotBlank(message = "El rol es obligatorio")
    private String rol;
}
