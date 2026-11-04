-- Migración 1: estructura de la base de datos de la tienda (gestión con Flyway)
-- Coherente con las entidades del módulo de catálogo:
--   catalogs ← products (FK catalog_id); price con escala 2, como el value object Money.

CREATE TABLE catalogs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(80)  NOT NULL UNIQUE,
    description VARCHAR(240) NOT NULL
);

CREATE TABLE products (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    catalog_id  BIGINT       NOT NULL,
    name        VARCHAR(80)  NOT NULL,
    description VARCHAR(240) NOT NULL,
    price       DECIMAL(8,2) NOT NULL,
    stock       INT          NOT NULL,
    FOREIGN KEY (catalog_id) REFERENCES catalogs (id)
);
