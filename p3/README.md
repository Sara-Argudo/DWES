# La tienda — P3 DWS (Partida: la tienda por capas)

Práctica **P3** de *Desarrollo Web en Entorno Servidor* (2.º DAW): el punto de arranque
de la partida, construido como se sabía hacer al terminar 1.º DAW —
**Spring Boot 4**, **arquitectura por capas**, persistencia **JDBC** sobre **MariaDB**
y **un único endpoint**: `GET /api/products`.

Enunciado: <https://cesguiro.es/doku.php/clase/daw/dws/practicas/p3>.

La funcionalidad reusable proviene de la práctica anterior (p1), donde el hito de
partida ya se había construido como práctica guiada; P3 la reestructura según el
árbol de paquetes que fija este enunciado: la implementación del repositorio vive
ahora en `persistence`, la capa del dominio con su interfaz.

## En qué consiste

Una API de catálogo mínima con un solo endpoint que devuelve el listado completo de
productos de la semilla de la tienda (3 catálogos y 28 productos), cada uno con su
`catalogId`. El resto de la API (catálogos, pedidos, usuarios…) no existe todavía:
se construye en los hitos posteriores. El listado plano es deliberadamente ingenuo:
en el hito 04 lo sustituye el listado paginado por catálogo.

## Organización del proyecto

Árbol de paquetes (`es.cesguiro.shop`), por capas con la inversión de dependencias
del repositorio: la **interfaz**(en `domain`) y la **implementación** (en `persistence`):

```text
src/main/java/es/cesguiro/shop
├── ShopApplication.java          # clase de arranque de Spring Boot
├── controller                    # capa web
│   └── ProductController.java    # GET /api/products
├── domain                        # modelo, servicio y PUERTO del repositorio
│   ├── Product.java              # referencia su catálogo por catalogId
│   ├── ProductService.java       # interfaz del servicio
│   ├── ProductServiceImpl.java
│   └── ProductRepository.java    # interfaz del repositorio: no menciona JDBC
└── persistence                   # implementa la interfaz sobre un DAO JDBC
    ├── ProductRepositoryImpl.java  # traduce la entidad al modelo dominio
    ├── ProductDao.java
    ├── ProductDaoJdbc.java       # PreparedStatement + ResultSet → entidad
    └── ProductEntity.java        # refleja la tabla products
```

Reglas de dependencia que respeta el código:

- `domain` no menciona JDBC ni detalles de acceso a datos: define la interfaz
  `ProductRepository` que necesita y el servicio la consulta.
- `persistence` implementa esa interfaz sobre un **DAO con JDBC**
  (`PreparedStatement`), como en 1.º DAW; `ProductRepositoryImpl` traduce la
  entidad `ProductEntity` al modelo `Product`.
- `controller` expone el listado plano en JSON (`List<Product>`), tal como
  fija el enunciado; la API del servicio es la frontera del dominio.

La relación de cada producto con su catálogo es, por ahora, solo el `catalogId`:
la integridad la pone la clave ajena, no el código. El precio y el stock son
números «sueltos»; las invariantes llegarán como value objects en el hito 05.

## Requisitos

- **JDK 25** (compilación y ejecución). El proyecto fija `<java.version>25</java.version>`.
- **Docker + Compose** para la base de datos MariaDB.
- Maven **no hace falta instalarlo**: el proyecto incluye el **Maven Wrapper**
  (`mvnw` / `mvnw.cmd`).

> **Nota sobre el puerto 3306:** el enunciado fija MariaDB en el puerto local `3306`,
> pero en este equipo el servicio Windows `MySQL80` ya ocupa ese puerto. Por eso el
> `compose.yaml` mapea el contenedor en el **`3307`** (`3307:3306`) y la URL de
> conexión usa `localhost:3307`, igual que en las prácticas anteriores. Si un día
> ese MySQL se detiene, basta con cambiar `3307:3306` por `3306:3306` en
> `compose.yaml` y restaurar el `3306` en `application.properties`.

## Arranque

1. Levantar la base de datos (crea las tablas y carga la semilla automáticamente):

   ```bash
   docker compose up -d
   ```

   Se crea la base `shop` con el usuario `shop` / contraseña `shop` (contraseñas de
   entorno de pruebas definidas por el enunciado, no secretos personales).
   La semilla (`db/init.sql`) deja **3 catálogos y 28 productos**.

2. Arrancar la aplicación:

   ```bash
   ./mvnw spring-boot:run        # Linux/macOS
   .\mvnw.cmd spring-boot:run    # Windows
   ```

3. Comprobar el endpoint:

   ```bash
   curl http://localhost:8080/api/products
   ```

   Devuelve el JSON de los 28 productos con su `id`, `catalogId`, `name`,
   `description`, `price` y `stock`.

## Compilación y pruebas

- Compilar: `./mvnw package` (o `.\mvnw.cmd package` en Windows).
- Pruebas: en este hito las pruebas son **manuales** (curl contra el endpoint);
  el primer criterio de pruebas automatizadas llega en el hito 05. Por eso no hay
  tests automatizados todavía.

## Comandos útiles

```bash
docker compose up -d     # levantar MariaDB
docker compose down      # parar MariaDB
docker compose down -v   # pararla y borrar el volumen de datos (vuelve a sembrar)
```
