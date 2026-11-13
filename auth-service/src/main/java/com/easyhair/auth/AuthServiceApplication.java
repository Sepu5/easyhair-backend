package com.easyhair.auth;

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
        info = @Info(title = "EasyHair - auth-service", version = "1.0", description = "Registro, login y perfil de usuarios. Entrega el token JWT."),
        // "/" = las pruebas de Swagger salen por el gateway (no directo al contenedor)
        servers = @Server(url = "/", description = "Gateway"),
        security = @SecurityRequirement(name = "token"))
// botón "Authorize" de Swagger para pegar el token del login
@SecurityScheme(name = "token", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
