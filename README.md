# DWES

**Desarrollo Web en Entorno Servidor** — 2.º DAW (Desarrollo de Aplicaciones Web).

Asignatura del segundo curso del ciclo formativo de grado superior *Desarrollo de Aplicaciones Web* (DAW), centrada en la construcción de aplicaciones que se ejecutan en el servidor: arquitectura, desarrollo de APIs REST, persistencia, seguridad y organización del código.

## En qué consiste

La asignatura trabaja sobre una **aplicación real y completa** (una *tienda* con catálogos, productos, pedidos y usuarios) que evoluciona hito a hito a lo largo del curso. Cada hito incorpora los conceptos nuevos al mismo proyecto, de modo que se ve la evolución del diseño: de un único endpoint «ingenuo» a una aplicación modular, validada, persistida con JPA y asegurada con Spring Security.

## Tecnologías del curso

| Tecnología | Uso |
| --- | --- |
| **Java (JDK 25)** | Lenguaje de desarrollo |
| **Spring Boot 4** | Framework del servidor |
| **Maven** | Gestión del proyecto, plugins y generación de código |
| **MariaDB (Docker + Compose)** | Base de datos y entorno |
| **JDBC / JPA (Spring Data)** | Persistencia |
| **OpenAPI / Swagger** | Diseño y documentación de contratos (API First) |
| **Spring Security + JWT** | Autenticación y autorización |
| **Testcontainers, ArchUnit, Spring Modulith** | Pruebas y organización modular |

## Contenido temático

1. Arquitecturas de software
2. API First
3. Validación e invariancias
4. Monolito modular (módulos y Spring Modulith)
5. Persistencia con Spring JPA
6. Login con JWT
7. Integración con Spring Security
8. Eventos de aplicación y mensajería
9. Logs e instrumentación
10. Serverless: Lambdas en local
11. Concurrencia: bloqueante, reactiva y Loom

## Organización de este repositorio

Cada práctica se desarrolla en una **rama propia**, conectada al hito correspondiente de la asignatura:

| Rama | Práctica |
| --- | --- |
| `main` | Portada de la asignatura (este README) |
| `tienda` | Aplicación de la tienda |
| `tienda/p1` | **P1 — Introducción: la tienda** (hito de partida: Spring Boot por capas, JDBC + MariaDB) |

A medida que avance el curso se irán añadiendo ramas para los sucesivos hitos (`p2`… `p9`), siempre partiendo del código de la práctica anterior.

## Referencias

- Apuntes y prácticas de la asignatura: <https://cesguiro.es/doku.php/clase/daw/dws/start>
