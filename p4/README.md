# La tienda — P4 DWS (API First: el contrato antes del código)

Práctica **P4** de *Desarrollo Web en Entorno Servidor* (2.º DAW): la API completa
del módulo de catálogo se **diseña y se revisa antes** de implementarla. El
contrato — un documento **OpenAPI** (`src/main/resources/openapi.yaml`, versión
**0.1.0**) — es ahora la fuente de verdad, y el esqueleto del código (interfaces
de la API y esquemas) se genera con **openapi-generator**. **Swagger UI**
(**springdoc**) hace el contrato navegable y ejecutable.

Enunciado: <https://cesguiro.es/doku.php/clase/daw/dws/practicas/p4>.

## En qué consiste

El módulo de catálogo gana la API completa: catálogos y productos, con el
listado **paginado** por catálogo en lugar del listado plano de la partida
(`GET /api/products` desaparece: veintiocho productos en una sola respuesta no
escalan y no deja navegar por catálogos).

| Operación | Petición | Respuestas |
| --- | --- | --- |
| Listado de catálogos | `GET /api/catalogs` | 200 · `Catalog`[] |
| Ficha de catálogo | `GET /api/catalogs/{id}` | 200 · 404 |
| Alta de catálogo | `POST /api/catalogs` | 201 |
| Modificación de catálogo | `PUT /api/catalogs/{id}` | 200 · 404 · 409 |
| Borrado de catálogo | `DELETE /api/catalogs/{id}` | 204 · 404 · 409 |
| Productos de un catálogo | `GET /api/catalogs/{id}/products?page&size` | 200 · 404 |
| Ficha de producto | `GET /api/products/{id}` | 200 · 404 |
| Alta de producto | `POST /api/products` | 201 · 404 |
| Modificación de producto | `PUT /api/products/{id}` | 200 · 404 |
| Borrado de producto | `DELETE /api/products/{id}` | 204 · 404 |

- Los **404** cubren catálogo o producto inexistente y el producto cuyo
  `catalogId` apunta a un catálogo que no existe.
- Los **409** cubren nombre de catálogo repetido (al crear o modificar) y
  catálogo con productos al intentar borrarlo (la comprobación, antes del borrado).
- Los errores se responden como documento **`Problem`** (RFC 9457):
  `type`, `title`, `status` y `detail`, con `application/problem+json`.
- La **paginación** se define en el contrato: `page` (desde 0, por defecto 0) y
  `size` (por defecto 10); la respuesta `ProductPage` lleva siempre los
  metadatos `page`, `size`, `totalElements` y `totalPages`.
- Aún **sin** constraints de validación de formato: llegan en el hito 05.

## Organización del proyecto

Árbol de paquetes (`es.cesguiro.shop`), como la partida por capas, con el
esqueleto **generado** del contrato:

```text
src/main/java/es/cesguiro/shop
├── ShopApplication.java            # clase de arranque de Spring Boot
├── api                             # (GENERADO, no se edita a mano)
│   ├── CatalogsApi.java            # interfaces ...Api : el contrato en Java
│   ├── ProductsApi.java
│   └── model                       # esquemas del contrato: Catalog,
│                                   #   CatalogRequest, Product, ProductRequest,
│                                   #   ProductPage y Problem
├── controller                      # capa web: IMPLEMENTA las interfaces generadas
│   ├── CatalogController.java      # catálogos + subrecurso de productos paginados
│   ├── ProductController.java      # ficha/alta/modificación/borrado de productos
│   └── RestExceptionHandler.java   # excepciones del dominio → Problem (RFC 9457)
├── domain                          # modelos, servicios y PUERTOS del repositorio
│   ├── Catalog.java                # Catálogo/Producto de la tienda
│   ├── CatalogService(.Impl)       # reglas de estado del contrato (409)
│   ├── CatalogRepository.java      # interfaz del repositorio: no menciona JDBC
│   ├── Product.java                # referencia su catálogo por catalogId
│   ├── ProductService(.Impl)       # 404 si el catálogo de los datos no existe
│   ├── ProductRepository.java
│   └── *NotFoundException /       # excepciones del estado (404 y 409)
│       DuplicateCatalogNameException / CatalogNotEmptyException
└── persistence                     # implementa los puertos sobre un DAO JDBC
    ├── CatalogDao(.Jdbc)           # PreparedStatement + ResultSet → entidad
    ├── CatalogEntity.java
    ├── CatalogRepositoryImpl.java
    ├── ProductDao(.Jdbc)           # la paginación: LIMIT/OFFSET a mano
    ├── ProductEntity.java
    └── ProductRepositoryImpl.java
```

Reglas que respeta el código (continuidad con la partida):

- `openapi.yaml` manda: cualquier cambio de la API se escribe primero en el
  contrato y se regenera con `mvnw compile`. El esqueleto generado vive en
  `target/generated-sources/openapi` y **nunca se edita a mano**.
- Los controladores **implementan** las interfaces `...Api` generadas: sus
  firmas vienen del contrato; la doc de /v3/api-docs sale de allí.
- `domain` no menciona JDBC: define las interfaces de los repositorios; los
  servicios aplican las reglas de estado del contrato (404 y 409).
- `persistence` implementa esos puertos sobre **DAOs JDBC** (`PreparedStatement`),
  como en 1.º DAW; el corte de página es `LIMIT`/`OFFSET` a mano, sin Spring Data
  (llega con el hito 07). `ProductRepositoryImpl` traduce `page`/`size` a
  `limit`/`offset`.

## Requisitos

- **JDK 25** (compilación y ejecución). El proyecto fija `<java.version>25</java.version>`.
- **Docker + Compose** para la base de datos MariaDB.
- Maven **no hace falta instalarlo**: el proyecto incluye el **Maven Wrapper**
  (`mvnw` / `mvnw.cmd`).

> **Nota sobre el puerto 3306:** el servicio Windows `MySQL80` de este equipo
> ocupa el 3306, por lo que el `compose.yaml` mapea MariaDB en el **3307**
> (`3307:3306`) y la URL de conexión usa `localhost:3307`, igual que en las
> prácticas anteriores.
>
> **Nota sobre el puerto 8080:** si otro proceso lo ocupa, la aplicación se
> puede arrancar en otro puerto con
> `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"`.

## Arranque

1. Levantar la base de datos (crea las tablas y carga la semilla automáticamente):

   ```bash
   docker compose up -d
   ```

   Se crea la base `shop` con el usuario `shop` / contraseña `shop` (contraseñas
   de entorno de pruebas definidas por el enunciado, no secretos personales).
   La semilla (`db/init.sql`) deja **3 catálogos y 28 productos**.

2. Arrancar la aplicación (regenera el esqueleto del contrato y compila):

   ```bash
   ./mvnw spring-boot:run        # Linux/macOS
   .\mvnw.cmd spring-boot:run    # Windows
   ```

3. Abrir **Swagger UI** en <http://localhost:8080/swagger-ui.html> (redirige a
   `/swagger-ui/index.html`); muestra catálogos y productos (por tags) y permite
   ejecutar cada operación, incluida la paginación. El documento generado queda
   en <http://localhost:8080/v3/api-docs>.

## Comprobación (según el enunciado)

Con Swagger UI o con `curl`:

- `GET /api/catalogs` devuelve los 3 catálogos; `GET /api/catalogs/1`, su ficha.
- `GET /api/catalogs/1/products` reparte el catálogo `Periféricos` (12 productos)
  en **2 páginas** con `size` 10 (por defecto), con los metadatos
  `page: 0, size: 10, totalElements: 12, totalPages: 2`;
  con `page=1` devuelve los 2 últimos. Con `size=5`, 3 páginas.
- `DELETE /api/catalogs/1` responde **409** porque tiene productos;
  repetir el nombre de un catálogo al crearlo (`POST /api/catalogs`) o
  modificarlo (`PUT /api/catalogs/{id}`) también responde **409**.
- Peticiones a recursos inexistentes responden **404** con un documento
  `Problem` (`application/problem+json`), p. ej.
  `curl -i http://localhost:8080/api/catalogs/999`.
- El listado plano `GET /api/products` ya no existe.
- El esqueleto del contrato (interfaces y esquemas) está **generado** y no se
  ha editado a mano; el `openapi.yaml` está en el repositorio con
  `info.version` **0.1.0**.

## Compilación y pruebas

- Compilar y generar el esqueleto: `.\mvnw.cmd compile` (o `./mvnw compile`).
- Empaquetar: `.\mvnw.cmd package`.
- Pruebas: siguen siendo **manuales** (Swagger UI y `curl`); el primer criterio
  de pruebas automatizadas llega en el hito 05.

## Comandos útiles

```bash
docker compose up -d     # levantar MariaDB
docker compose down      # parar MariaDB
docker compose down -v   # pararla y borrar el volumen de datos (vuelve a sembrar)
```
