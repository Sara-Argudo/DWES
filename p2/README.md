# La tienda — P2 DWS (implementación final)

Práctica **P2** de *Desarrollo Web en Entorno Servidor* (2.º DAW): la **implementación
final** de la tienda, que condensa los hitos 04–09 sobre la base de la P1:

- **Catálogos y productos**: CRUD completo (solo ADMIN) con lectura pública,
  listado de productos **paginado** y corrección de errores 404/400/409.
- **Pedidos**: creación con **reserva atómica de stock**, captura del precio de
  cada línea en el momento de la reserva, cancelación que libera el stock
  (una sola vez) y consulta de los pedidos propios.
- **Usuarios y seguridad**: registro público (rol `USER` siempre), *login* con
  **JWT** (access + refresh con rotación), roles `USER`/`ADMIN` y
  **Spring Security** con reglas por rutas y método HTTP.
- **Errores RFC 9457**: todos los fallos (incluidos 401/403 de seguridad) en
  formato `application/problem+json`.
- **Value Objects**: `Money` (> 0, dos decimales) y `Stock` (≥ 0) con
  `AttributeConverter` para JPA.
- **Pruebas de integración** contra **MariaDB real con Testcontainers**.

Enunciado: <https://cesguiro.es/doku.php/clase/daw/dws/practicas/p2>.

## Stack

- **Java 25** y **Spring Boot 4.1** (starters `webmvc`, `data-jpa`, `security`,
  `validation`), **Maven** (wrapper incluido).
- **MariaDB 12** (misma imagen en Docker Compose y en Testcontainers).
- **jjwt 0.13** para el access token HS256 y el refresh token (se guarda solo su
  hash SHA-256, con rotación en cada refresh y en el logout).
- **Hibernate 7** con `ddl-auto=none`: el esquema los gestiona `db/init.sql`.

## Estructura (arquitectura modular)

```
src/main/java/es/cesguiro/shop/
├── ShopApplication.java
├── catalog/            # catálogos y productos
│   ├── api/            # puerto público del módulo (CatalogApi) — lo usa orders
│   ├── application/    # servicios (CatalogService, ProductService)
│   ├── controller/     # REST + DTOs
│   ├── domain/         # entidades (Catalog, Product) y excepciones
│   └── infrastructure/persistence/  # repositorios Spring Data
├── orders/             # pedidos (idéntica distribución)
├── users/              # usuarios + auth + JWT + Spring Security
└── shared/             # Money, Stock (value objects) y manejo de errores
```

La frontera entre módulos es explícita: `orders` no toca las entidades de
`catalog`; sólo ve el puerto `CatalogApi` (`priceOf`, `reserve`, `release`),
que implementa `catalog` con `UPDATE`s condicionales atómicos.

## Arranque

1. Levanta MariaDB (puerto **3307**, para no chocar con MySQL del sistema):

   ```
   docker compose up -d
   ```

2. Ejecuta la aplicación (crea las tablas y siembra catálogos, productos y el
   ADMIN al primer arranque):

   ```
   .\mvnw.cmd spring-boot:run
   ```

   o:

   ```
   & "C:\ruta\a\jdk-25\bin\java.exe" -jar target\shop-0.0.1-SNAPSHOT.jar
   ```

### Configuración por variables de entorno

Todos los valores sensibles tienen un *default* de desarrollo (`application.properties`);
en producción se sobre escriben con variables de entorno:

| Variable | Uso | Default |
|---|---|---|
| `SHOP_ADMIN_EMAIL` / `SHOP_ADMIN_PASSWORD` | semilla del ADMIN | `admin@shop.local` / `admin-pass-123` |
| `SHOP_JWT_SECRET` | firma HS256 de los access tokens | secreto de desarrollo (no usar en real) |
| `SHOP_JWT_TTL_MINUTES` / `SHOP_REFRESH_TTL_DAYS` | vida de los tokens | `60` / `7` |

## Datos iniciales

- **3 catálogos** (Periféricos, Pantallas, Accesorios) y **28 productos**, por
  `db/init.sql`.
- **Usuario ADMIN** creado al arrancar (`admin@shop.local` / `admin-pass-123`
  por defecto, si aún no existe).
- **Usuarios USER**: se registran por `POST /api/users` (el rol lo fija el
  servidor, siempre `USER`).

## API (resumen con ejemplos)

Todos los errores van en formato Problem RFC 9457 (`application/problem+json`).

### Catálogo (lecturas públicas)

```
GET /api/catalogs                       # lista de catálogos
GET /api/catalogs/{id}                  # un catálogo (404 si no existe)
GET /api/catalogs/{id}/products?page=0&size=5   # productos paginados
GET /api/products/{id}                  # un producto (404 si no existe)
```

### Escrituras (solo ADMIN; sin token → 401, USER → 403)

```
POST   /api/catalogs        {"name": "...", "description": "..."}
PUT    /api/catalogs/{id}   (mismo cuerpo)
DELETE /api/catalogs/{id}   → 409 si el catálogo tiene productos

POST   /api/products        {"catalogId": 1, "name": "...", "description": "...", "price": 9.99, "stock": 10}
PUT    /api/products/{id}
DELETE /api/products/{id}
→ 409 si el nombre ya existe, 400 si el precio tiene > 2 decimales o el stock es negativo
```

### Registro, login y tokens

```
POST /api/users          {"name": "...", "email": "...", "password": "..."}  → 201 USER, 409 email duplicado
POST /api/auth/login     {"email": "...", "password": "..."}  → 401 si falla → accessToken + refreshToken
POST /api/auth/refresh   {"refreshToken": "..."}  → rota: el anterior queda revocado
POST /api/auth/logout    {"refreshToken": "..."}  → lo revoca también
```

### Pedidos (autenticados; identidad sacada del token, nunca del cuerpo)

```
POST /api/orders         {"lines": [{"productId": 21, "quantity": 2}]}
                         → 201; cada línea guarda su unitPrice; 409 si no hay stock
GET  /api/orders/{id}    → 200 (dueño o ADMIN); 403 si es de otro; 404 si no existe
POST /api/orders/{id}/cancel → 200; libera el stock; 409 si ya estaba cancelado
GET  /api/me/orders      → los pedidos del usuario autenticado
```

Ejemplo completo con curl:

```
curl -H "Content-Type: application/json" \
     -d '{"name":"Ana","email":"ana@shop.local","password":"secret-pass-1"}' \
     http://localhost:8080/api/users

curl -H "Content-Type: application/json" \
     -d '{"email":"ana@shop.local","password":"secret-pass-1"}' \
     http://localhost:8080/api/auth/login

curl -H "Authorization: Bearer <accessToken>" -H "Content-Type: application/json" \
     -d '{"lines":[{"productId":21,"quantity":2}]}' \
     http://localhost:8080/api/orders
```

## Pruebas

```
$env:JAVA_HOME = "C:\ruta\a\jdk-25"
.\mvnw.cmd test
```

Las pruebas de integración levantan un contenedor **MariaDB 12 real**
(Testcontainers) con el mismo esquema y semilla que en producción:

- `AuthApiTests` — registro, hash de contraseña, login, 401, rotación de refresh, logout.
- `CatalogApiTests` — CRUD de catálogos/productos, 409 por duplicados y catálogo
  no vacío, 400 de `Money`/`Stock`, 404, paginación y reglas ADMIN.
- `OrderApiTests` — creación con captura de precios y reserva de stock,
  rollback de todo si una reserva falla, cancelación con liberación,
  propiedad de los pedidos y doble cancelación.
- `ValueObjectsTests` — invariancias de `Money` y `Stock` (sin contenedor).
