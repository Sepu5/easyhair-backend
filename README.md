# easyhair-backend

Backend de **EasyHair** con microservicios **Spring Boot**. Lo usa la app Android ([lugrossolli-ux/EASYHAIR](https://github.com/lugrossolli-ux/EASYHAIR)) y más adelante la web de la administradora.

Proyecto de **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC).
Integrantes: Franco Sepúlveda Romero y Luis Grossoli Fica.

## Microservicios
| Servicio | Puerto | Esquema Supabase | Qué hace | Integrante |
|---|---|---|---|---|
| gateway-service | 8080 (80 en AWS) | — | Única entrada. Rutas a cada servicio y validación del token JWT | Franco |
| auth-service | 8081 | autenticacion | Registro, login (BCrypt + JWT), perfil y rol CLIENTE/STAFF | Franco |
| catalogo-service | 8082 | catalogo | Servicios, precios y peluqueros | Luis |
| reservas-service | 8083 | reservas | Disponibilidad, reservas, estados (atender, no asistió, cancelar, reprogramar) y bloqueos | Franco |
| inventario-service | 8084 | inventario | Materiales, consumos del staff con aprobación y compras | Franco |
| finanzas-service | 8085 | finanzas | Ingresos y egresos, resúmenes por mes, peluquero y categoría | Luis |

Comunicación entre servicios (RestClient):
- reservas → catalogo: buscar un peluquero libre cuando el cliente elige "sin preferencia".
- reservas → finanzas: registrar el **ingreso** cuando se atiende una reserva.
- inventario → finanzas: registrar el **egreso** cuando se compra material.

Los errores se responden como `{ "mensaje": "..." }` con 400, 401, 404 o 409.

## Tecnologías
Java 21 · Spring Boot 4 · Spring Data JPA · Spring Cloud Gateway · jjwt · BCrypt · PostgreSQL (Supabase) · springdoc-openapi (Swagger) · JUnit 5 · Docker y docker compose · AWS EC2.

Cada servicio sigue la misma estructura: `entities`, `repository`, `services` (interfaz + Impl), `controller` y `dto`.

## En la nube
- **AWS EC2** t3.medium (Ubuntu 24.04) con **IP elástica** `54.152.113.142`.
- Los 6 servicios corren con `docker compose`; solo el gateway queda expuesto en el **puerto 80**.
- API: `http://54.152.113.142/api/...`
- **Swagger:** `http://54.152.113.142/swagger-ui.html` (un selector por microservicio; botón *Authorize* para pegar el token del login).

## Cómo ejecutar en local
1. Copiar `.env.example` a `.env` en la raíz y completar los datos de Supabase y `JWT_SECRET` (el `.env` no se sube al repositorio).
2. Con Docker Desktop abierto:
   ```
   docker compose up -d --build
   docker compose ps
   ```
3. Probar en `http://localhost/api/servicios` y la documentación en `http://localhost/swagger-ui.html`.

Para correr un servicio solo (sin Docker): entrar a su carpeta y ejecutar `./mvnw spring-boot:run`.

## Actualizar el servidor después de un cambio
```
cd easyhair-backend
git pull
sudo docker compose up -d --build
```

## Pruebas
Pruebas unitarias **JUnit** (sin Mockito) de la lógica de cada servicio. Se corren fuera del servidor:
```
cd reservas-service
./mvnw test
```
| Servicio | Pruebas |
|---|---|
| auth-service | 7 |
| catalogo-service | 7 |
| reservas-service | 8 |
| inventario-service | 8 |
| finanzas-service | 9 |
| gateway-service | 6 |

Además, cada servicio se probó con **Postman** y el flujo completo se probó contra Docker local y contra AWS (15/15 OK). Las capturas están en las tarjetas de Trello.

## Trabajo colaborativo
Git Flow: `main` con las versiones entregadas, `desarrollo` para juntar todo y una rama `tarea/...` por cada tarea, con Pull Request revisado por el compañero.

| Rama | Qué se hizo | Integrante |
|---|---|---|
| `tarea/auth-service` | auth-service con registro, login, perfil y pruebas | Franco Sepúlveda |
| `tarea/reservas-service` | reservas-service con disponibilidad, estados y bloqueos | Franco Sepúlveda |
| `tarea/gateway-service` | Gateway con rutas y validación del token | Franco Sepúlveda |
| `tarea/inventario-service` | inventario-service con consumos, aprobación y compras | Franco Sepúlveda |
| `tarea/catalogo-service` | catalogo-service y finanzas-service | Luis Grossoli |
| `tarea/docker-catalogo-finanzas` | Dockerfiles de catalogo y finanzas | Luis Grossoli |
| `tarea/docker` | Dockerfiles de auth, reservas, inventario y gateway, y docker-compose | Franco Sepúlveda |
| `tarea/swagger` | Documentación Swagger de todos los servicios | Franco Sepúlveda |
| `tarea/readme` | Este README | Franco Sepúlveda |
