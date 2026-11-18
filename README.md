# Parcial 3 — Backend (Full Stack II)

API REST de la **Evaluación Parcial 3** de DSY1104 *Desarrollo Full Stack II* (Duoc UC).
Implementa autenticación con **JWT**, autorización por **roles** y persistencia en base de datos relacional.

> Frontend del proyecto: [SAAB85/FULLSTACK_PARCIAL3_FRONTEND](https://github.com/SAAB85/FULLSTACK_PARCIAL3_FRONTEND)

## Stack

| Capa | Tecnología |
| --- | --- |
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1 · Spring Security · Spring Data JPA |
| Base de datos | H2 en archivo (`backend/data/`) |
| Autenticación | JWT firmado con HS256 (JJWT 0.12) |
| Documentación | springdoc-openapi (Swagger UI) |
| Pruebas | JUnit 5 · MockMvc |

## Cómo ejecutar

```bash
./mvnw spring-boot:run
```

- API: <http://localhost:8080/api>
- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- Consola H2: <http://localhost:8080/h2-console> (`jdbc:h2:file:./data/ep3db`, usuario `sa`, sin contraseña)

Los datos se guardan en `data/` y sobreviven al reinicio. Al primer arranque se crean
los usuarios y productos de demostración.

## Usuarios de prueba

| Rol | Usuario | Contraseña | Permisos |
| --- | --- | --- | --- |
| ADMIN | `admin` | `admin123` | Productos y usuarios; elimina productos |
| VENDEDOR | `vendedor` | `vendedor123` | Crea y edita productos; no elimina ni ve usuarios |
| CLIENTE | `cliente` | `cliente123` | Solo consulta productos |

## Endpoints

### Probar en Swagger UI

1. Ejecuta `POST /api/auth/login` con un usuario de prueba y copia el campo `token`.
2. Pulsa **Authorize** y pega el token **sin** el prefijo `Bearer`.
3. Con el rol ADMIN puedes probar las cinco operaciones de **Productos** con **Try it out**.
   Para crear un producto envía `codigo`, `nombre`, `categoria`, `precio` y `stock`;
   `descripcion` es opcional y el servidor genera el ID.

| Método | Ruta | Acceso |
| --- | --- | --- |
| POST | `/api/auth/login` | Público |
| GET | `/api/auth/me` | Autenticado |
| GET | `/api/productos` | Autenticado |
| GET | `/api/productos/{id}` | Autenticado |
| POST | `/api/productos` | ADMIN · VENDEDOR |
| PUT | `/api/productos/{id}` | ADMIN · VENDEDOR |
| DELETE | `/api/productos/{id}` | ADMIN |
| GET | `/api/usuarios` | ADMIN |

### Ejemplo de login

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

Respuesta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "username": "admin",
  "nombre": "Administrador",
  "rol": "ADMIN",
  "expiraEn": 7200000
}
```

Para consultar la API con el token:

```bash
curl -s http://localhost:8080/api/productos \
  -H "Authorization: Bearer <TOKEN>"
```

## Cómo funciona la seguridad

1. El usuario envía sus credenciales a `POST /api/auth/login`.
2. El backend autentica con `AuthenticationManager` y genera un JWT firmado que incluye
   `sub` (usuario), `rol` y `nombre`.
3. El frontend guarda la respuesta en `localStorage` y adjunta `Authorization: Bearer <token>`
   en cada petición.
4. `JwtAuthenticationFilter` valida la firma y la expiración antes de permitir el acceso.
5. Las reglas se aplican en el backend con `@PreAuthorize`. Ocultar un botón en el frontend
   no es seguridad; la API siempre valida.

### Configuración

| Variable | Uso | Valor por defecto |
| --- | --- | --- |
| `JWT_SECRET` | Clave de firma (mínimo 32 caracteres) | Clave de desarrollo incluida |
| `JWT_EXPIRACION_MS` | Vigencia del token | `7200000` (2 horas) |
| `CORS_ORIGENES` | Orígenes permitidos | `http://localhost:5173,http://localhost:4173` |

> En producción define `JWT_SECRET` con una clave propia. La clave por defecto es solo para desarrollo.

## Pruebas

```bash
./mvnw test
```

Las pruebas cubren:

- Login correcto e incorrecto.
- Acceso a productos con y sin token.
- Restricción de `/api/usuarios` al rol ADMIN.
- Creación de productos por VENDEDOR y eliminación solo por ADMIN.

## Estructura

```
src/main/java/cl/duoc/fullstack/ep3/
├── config/        Datos iniciales de demostración
├── controller/    Endpoints REST
├── dto/           Contratos de entrada y salida
├── exception/     Manejo uniforme de errores
├── model/         Entidades JPA (Usuario, Producto, Rol)
├── repository/    Repositorios Spring Data
├── security/      JWT, filtro y configuración de seguridad
└── service/       Reglas de negocio
```
