package com.easyhair.catalogo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// título y descripción que se ven en Swagger
@OpenAPIDefinition(
        info = @Info(title = "EasyHair - catalogo-service", version = "1.0", description = "Servicios de la peluquería, precios y peluqueros."),
        // "/" = las pruebas de Swagger salen por el gateway (no directo al contenedor)
        servers = @Server(url = "/", description = "Gateway"),
        security = @SecurityRequirement(name = "token"))
// botón "Authorize" de Swagger para pegar el token del login
@SecurityScheme(name = "token", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@SpringBootApplication
public class CatalogoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogoServiceApplication.class, args);
	}

}
