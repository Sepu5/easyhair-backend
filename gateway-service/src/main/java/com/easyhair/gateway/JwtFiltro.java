package com.easyhair.gateway;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

// revisa el token de cada petición antes de mandarla al microservicio
@Component
public class JwtFiltro implements GlobalFilter, Ordered {

    private final SecretKey clave;

    public JwtFiltro(@Value("${jwt.secret}") String secret) {
        this.clave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ruta = exchange.getRequest().getURI().getPath();
        HttpMethod metodo = exchange.getRequest().getMethod();

        if (esPublica(ruta, metodo)) {
            return chain.filter(exchange);
        }

        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return rechazar(exchange, "Debes iniciar sesión");
        }

        try {
            String token = header.substring(7);
            Claims datos = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();

            // le pasamos a los microservicios quién es y qué rol tiene
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-Usuario-Id", String.valueOf(datos.get("id")))
                    .header("X-Usuario-Rol", String.valueOf(datos.get("rol")))
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (Exception e) {
            return rechazar(exchange, "Sesión inválida o expirada, inicia sesión de nuevo");
        }
    }

    // login, registro y ver el catálogo no necesitan token
    public boolean esPublica(String ruta, HttpMethod metodo) {
        if (ruta.startsWith("/api/auth/")) {
            return true;
        }
        if (HttpMethod.OPTIONS.equals(metodo)) {
            return true;
        }
        // rutas con ".." nunca son públicas (evita saltarse el token con /docs/../api)
        if (ruta.contains("..")) {
            return false;
        }
        // la documentación de Swagger se puede ver sin iniciar sesión
        if (ruta.startsWith("/swagger-ui") || ruta.startsWith("/v3/api-docs")
                || ruta.startsWith("/webjars")
                || (ruta.startsWith("/docs/") && ruta.contains("/v3/api-docs"))) {
            return true;
        }
        boolean esCatalogo = ruta.startsWith("/api/servicios") || ruta.startsWith("/api/peluqueros");
        return esCatalogo && HttpMethod.GET.equals(metodo);
    }

    private Mono<Void> rechazar(ServerWebExchange exchange, String mensaje) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] json = ("{\"mensaje\":\"" + mensaje + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(json);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    // que se ejecute antes que todo lo demás
    @Override
    public int getOrder() {
        return -1;
    }
}
