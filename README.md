# Parcial 3 — Backend (Full Stack II)

API REST de la **Evaluación Parcial 3** de DSY1104 *Desarrollo Full Stack II* (Duoc UC).
Implementa el modelo de datos de una tienda, autenticación con **JWT**, autorización por **roles**
y persistencia en una base de datos relacional.

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

## Modelo de datos

```
USUARIO 1 ─── N BOLETA 1 ─── N DETALLE_BOLETA N ─── 1 PRODUCTO N ─── 1 CATEGORIA
```

- **USUARIO**: cuenta con rol ADMIN, VENDEDOR o CLIENTE.
- **CATEGORIA**: agrupa productos y no puede eliminarse si tiene productos asociados.
- **PRODUCTO**: código único, precio y stock; pertenece a una categoría.
- **BOLETA**: orden de compra de un cliente, con número, fecha, estado y total.
- **DETALLE_BOLETA**: producto, cantidad, precio unitario y subtotal. El precio se guarda como
  fotografía del momento de la compra.

Al crear una boleta, el backend valida el stock, lo descuenta y calcula el total.
Al anularla, repone el stock de todos sus detalles.

## Cómo ejecutar

```bash
./mvnw spring-boot:run
```

- API: <http://localhost:8080/api/v1>
- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- Consola H2: <http://localhost:8080/h2-console> (`jdbc:h2:file:./data/ep3db`, usuario `sa`, sin contraseña)

Los datos se guardan en `data/` y sobreviven al reinicio. Al primer arranque se crean
los usuarios, categorías y productos de demostración.

> Si vienes de la versión anterior del esqueleto (sin categorías), elimina la carpeta `data/`
> para que Hibernate genere el esquema nuevo y vuelva a cargar los datos de demostración.

## Usuarios de prueba

| Rol | Usuario | Contraseña | Permisos |
| --- | --- | --- | --- |
| ADMIN | `admin` | `admin123` | Acceso total: catálogo, categorías, boletas y usuarios |
| VENDEDOR | `vendedor` | `vendedor123` | Consulta productos y boletas |
| CLIENTE | `cliente` | `cliente123` | Tienda: consulta productos y crea sus boletas |

## Endpoints

### Probar en Swagger UI

1. Ejecuta `POST /api/v1/auth/login` con un usuario de prueba y copia el campo `token`.
2. Pulsa **Authorize** y pega el token **sin** el prefijo `Bearer`.
3. Para crear un producto obtén primero un `id` desde `GET /api/v1/categorias` y envíalo como
   `categoriaId`. El cuerpo requiere `codigo`, `nombre`, `categoriaId`, `precio` y `stock`;
   `descripcion` es opcional.
4. Para crear una boleta envía `items` con `productoId` y `cantidad`.

| Método | Ruta | Acceso |
| --- | --- | --- |
| POST | `/api/v1/auth/login` | Público |
| GET | `/api/v1/auth/me` | Autenticado |
| GET | `/api/v1/productos` | Autenticado |
| GET | `/api/v1/productos/{id}` | Autenticado |
| POST | `/api/v1/productos` | ADMIN |
| PUT | `/api/v1/productos/{id}` | ADMIN |
| DELETE | `/api/v1/productos/{id}` | ADMIN |
| GET | `/api/v1/categorias` | Autenticado |
| GET | `/api/v1/categorias/{id}` | Autenticado |
| POST | `/api/v1/categorias` | ADMIN |
| PUT | `/api/v1/categorias/{id}` | ADMIN |
| DELETE | `/api/v1/categorias/{id}` | ADMIN · solo si no tiene productos |
| GET | `/api/v1/boletas` | ADMIN y VENDEDOR: todas · CLIENTE: propias |
| GET | `/api/v1/boletas/{id}` | ADMIN y VENDEDOR: cualquiera · CLIENTE: propias |
| POST | `/api/v1/boletas` | ADMIN · CLIENTE |
| PATCH | `/api/v1/boletas/{id}/estado` | ADMIN |
| GET | `/api/v1/usuarios` | ADMIN |

### Ejemplo de login

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
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
curl -s http://localhost:8080/api/v1/productos \
  -H "Authorization: Bearer <TOKEN>"
```

### Ejemplo de boleta

```bash
curl -s -X POST http://localhost:8080/api/v1/boletas \
  -H "Authorization: Bearer <TOKEN_CLIENTE>" \
  -H 'Content-Type: application/json' \
  -d '{"items":[{"productoId":1,"cantidad":2}],"observacion":"Compra de prueba"}'
```

## Cómo funciona la seguridad

1. El usuario envía sus credenciales a `POST /api/v1/auth/login`.
2. El backend autentica con `AuthenticationManager` y genera un JWT firmado que incluye
   `sub` (usuario), `rol` y `nombre`.
3. El frontend guarda la respuesta en `localStorage` y adjunta `Authorization: Bearer <token>`
   en cada petición.
4. `JwtAuthenticationFilter` valida la firma y la expiración antes de permitir el acceso.
5. Las reglas se aplican en el backend con `@PreAuthorize` y validaciones de propiedad
   (un CLIENTE solo ve sus propias boletas). Ocultar un botón en el frontend no es seguridad;
   la API siempre valida.

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

Las 12 pruebas cubren:

- Login correcto e incorrecto.
- Acceso a productos con y sin token.
- Restricción de `/api/v1/usuarios` al rol ADMIN.
- Catálogo de solo lectura para VENDEDOR.
- CRUD de categorías solo para ADMIN y bloqueo de borrado con productos asociados.
- Creación de boletas por CLIENTE con descuento de stock y total calculado en el servidor.
- Rechazo por stock insuficiente.
- Visibilidad de boletas: VENDEDOR ve todas, CLIENTE solo las propias.
- Cambio de estado y reposición de stock al anular.

## Estructura

```
src/main/java/cl/duoc/fullstack/ep3/
├── config/        Datos iniciales de demostración y OpenAPI
├── controller/    Endpoints REST versionados en /api/v1
├── dto/           Contratos de entrada y salida
├── exception/     Manejo uniforme de errores
├── model/         Entidades JPA (Usuario, Categoria, Producto, Boleta, DetalleBoleta)
├── repository/    Repositorios Spring Data
├── security/      JWT, filtro y configuración de seguridad
└── service/       Reglas de negocio
```
