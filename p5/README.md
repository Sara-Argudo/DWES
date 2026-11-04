# La tienda — P5 DWS (Validación e invariancias)

Práctica **P5** de *Desarrollo Web en Entorno Servidor* (2.º DAW): con el CRUD
de catálogos y productos de [la práctica P4](../p4), la API ya existe y es
exactamente la del contrato; lo que falta es **rechazar correctamente lo que
no admite**. Este hito responde dos preguntas distintas, cada una en su
lugar:

| Pregunta | Dónde se responde | Mecanismo | Respuesta |
| --- | --- | --- | --- |
| ¿Cumple la petición el formato del contrato? | La petición de entrada, en la capa de presentación | **Jakarta Validation** (constraints declaradas en el contrato) | **400** con campo y motivo |
| ¿El estado propuesto es válido para el negocio? | El dominio | **Invariantes** con value objects (`Money`, `Stock`) | Excepción de dominio, mapeada al error del contrato |

Enunciado: <https://cesguiro.es/doku.php/clase/daw/dws/practicas/p5>.

## En qué consiste

1. **Constraints en el contrato.** `openapi.yaml` pasa a la versión **0.1.1**
   y declara lo que la API admite, así que las constraints se redactan en el
   propio documento y `openapi-generator` (con `useBeanValidation=true`) las
   traduce a anotaciones **Jakarta Validation** en los esquemas generados:

   | Esquema | Constraint |
   | --- | --- |
   | `CatalogRequest.name` | `minLength` 1 · `maxLength` 80 |
   | `CatalogRequest.description` | `maxLength` 240 |
   | `ProductRequest.name` | `minLength` 1 · `maxLength` 80 |
   | `ProductRequest.description` | `maxLength` 240 |
   | `ProductRequest.price` | `minimum` 0.01 · `maximum` 999999.99 |
   | `ProductRequest.stock` | `minimum` 0 · `maximum` 100000 |
   | `ProductRequest.catalogId` | `minimum` 1 |

2. **El 400 coherente con el contrato.** `RestExceptionHandler` responde los
   errores en formato **`Problem`** (RFC 9457, el esquema `Problem` del
   contrato): el **400** de validación lleva campo y motivo por cada
   violación, y las excepciones del dominio se mapean a sus estados
   (404 para lo inexistente, 409 para el estado incompatible).

3. **Invariantes con value objects.** El dominio gana los value objects
   `Money` (precio: mayor que cero, escala de 2) y `Stock` (entero no
   negativo): cada uno valida en su construcción y lanza la excepción de
   dominio propia (`InvalidValueException`). El modelo `Product` se compone
   con ellos; la creación y la modificación de productos (`ProductService`)
   **construyen primero los value objects**, así que un estado inválido
   nunca llega a la persistencia. Un mismo dato puede aparecer en las dos
   fronteras: `minimum` en el contrato (formato) **y** la validación del
   `Money` (invariante). No es redundancia: protegen fronteras distintas.

4. **Pruebas** (el primer criterio de pruebas automatizadas del proyecto):
   - **Value objects** (unitarias, sin Spring y sin base de datos):
     `MoneyTests` y `StockTests`.
   - **Servicio** (unitarias con un doble del repositorio, Mockito):
     `CatalogServiceTests` y `ProductServiceTests`.
   - **Presentación** (con el servicio doblado, MockMvc + `@WebMvcTest`):
     `CatalogControllerTests` y `ProductControllerTests`.

## Monolito modular

Además, el proyecto se reorganiza en un **monolito modular**: módulos
independientes con fronteras claras, una sola aplicación que los ejecuta y
la persistencia común en su sitio.

```text
p5/src/main/java/es/cesguiro/shop
├── persistence                     # FUERA de los módulos
│   └── ShopApplication.java        # única clase de arranque de Spring Boot
├── config                          # (reservada, ahora vacía)
├── controller                      # MÓDULO 1: catálogo (la tienda)
│   ├── CatalogController.java      # implementa la interfaz CatalogsApi generada
│   ├── ProductController.java      # implementa la interfaz ProductsApi generada
│   ├── dto                         # los DTO del contrato (GENERADOS por
│   │                               #   openapi-generator, en controller.dto)
│   ├── mapper                      # traducción DTO ↔ dominio
│   │   ├── CatalogMapper.java
│   │   └── ProductMapper.java
│   ├── exception                   # presentación: Problem (RFC 9457)
│   │   └── RestExceptionHandler.java
│   └── domain                      # dominio del módulo
│       ├── model                   # Catalog, Product + Money, Stock (VO)
│       ├── exception               # excepciones propias del dominio
│       ├── repository              # puertos: no mencionan JDBC
│       ├── service                 # reglas de estado y construcción por VO
│       └── persistence             # DAOs JDBC que implementan los puertos
└── order                           # MÓDULO 2: pedidos (andamiajo, hito 06)
    ├── controller
    │   └── OrderController.java        # prepara los endpoints del hito 06
    ├── domain
    │   └── model                   # Order, OrderDetail
    └── service                     # (reservada, ahora vacía)
```

Reglas de la arquitectura:

- Dos módulos con fronteras claras (`controller` y `order`); el módulo de
  pedidos no depende del de catálogo (ni al revés).
- Una única aplicación (`ShopApplication`) en `persistence`, fuera de los
  módulos; el escaneo arranca en `es.cesguiro.shop`.
- Los DTO de la presentación son los esquemas del contrato **generados** en
  el paquete `controller.dto` (la carpeta se materializa en
  `target/generated-sources/openapi`); no se duplican DTO a mano.
- `domain` define los puertos del repositorio; `domain/persistence` los
  implementa sobre DAOs JDBC (LIMIT/OFFSET a mano, sin Spring Data).

## Base de datos con Flyway

La estructura y la semilla de la base de datos pasan a gestionarse con
**Flyway** (migraciones en `src/main/resources/db/migration`):

| Migración | Contenido |
| --- | --- |
| `V1__catalogs_and_products_schema.sql` | tablas `catalogs` y `products` (FK `catalog_id`) |
| `V2__catalogs_and_products_seed.sql` | semilla: **3 catálogos y 28 productos** (los datos que se conservan) |

El `compose.yaml` ya **no** monta `init.sql`: las tablas y los datos las
ejecuta Flyway al arrancar la aplicación, así que no hay duplicaciones ni
conflictos entre scripts. Si el volumen ya arrastraba la base antigua,
resémbala con `docker compose down -v` antes de arrancar.

## Requisitos

- **JDK 25** (compilación y ejecución). El proyecto fija `<java.version>25</java.version>`.
- **Docker + Compose** para la base de datos MariaDB.
- Maven **no hace falta instalarlo**: el proyecto incluye el **Maven Wrapper**
  (`mvnw` / `mvnw.cmd`).

> **Nota sobre el puerto 3306:** el servicio Windows `MySQL80` de este equipo
> ocupa el 3306, por lo que el `compose.yaml` mapea MariaDB en el **3307**
> (`3307:3306`) y la URL de conexión usa `localhost:3307`, igual que en las
> prácticas anteriores.

## Arranque

1. Levantar la base de datos:

   ```bash
   docker compose up -d
   ```

2. Arrancar la aplicación (regenera el esqueleto del contrato y **ejecuta las
   migraciones de Flyway**): 

   ```bash
   ./mvnw spring-boot:run        # Linux/macOS
   .\mvnw.cmd spring-boot:run    # Windows
   ```

3. Abrir **Swagger UI** en <http://localhost:8080/swagger-ui.html>; muestra
   los constraints del contrato en los esquemas de petición. El documento
   generado queda en <http://localhost:8080/v3/api-docs>.

## Comprobación (según el enunciado)

Con Swagger UI o con `curl`:

- Una petición con **precio 0** o **stock negativo** responde **400** con
  campo y motivo, antes de tocar el dominio:

  ```bash
  curl -i -X POST http://localhost:8080/api/products -H "Content-Type: application/json" \
    -d '{"catalogId":1,"name":"Cable HDMI","description":"2 m","price":0,"stock":10}'
  ```

- Un **catálogo con nombre vacío** responde **400** por la misma vía.
- El **mismo rango está garantizado por los value objects** en pruebas
  unitarias: construir un `Money` con precio 0 lanza la excepción de dominio.
- Un catálogo o producto **inexistente** en ficha, modificación o borrado
  responde **404** con el problema coherente; el nombre de catálogo repetido
  y el catálogo con productos, **409**.
- El error 400 muestra **campo y motivo** en el formato `Problem`
  (`application/problem+json`): `type`, `title`, `status`, `detail`.
- Versión del contrato: **0.1.1**.

## Compilación y pruebas

```bash
docker compose up -d     # levantar MariaDB
compose down -v          # pararla y borrar el volumen (vuelve a sembrar Flyway)

.\mvnw.cmd compile       # regenerar esqueleto + compilar
.\mvnw.cmd test          # pruebas unitarias (VO, servicios, presentación)
.\mvnw.cmd package       # empaquetar
```

Las pruebas **no necesitan base de datos**: los value objects y los
servicios se prueban con dobles y la presentación con MockMvc.

## Comandos útiles

```bash
docker compose up -d     # levantar MariaDB
docker compose down      # parar MariaDB
docker compose down -v   # pararla y borrar el volumen de datos
```
