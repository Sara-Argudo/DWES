package es.cesguiro.shop.controller.domain.exception;

/**
 * Excepción del dominio: el nombre del catálogo ya lo usa otro catálogo (409).
 */
public class DuplicateCatalogNameException extends RuntimeException {

    public DuplicateCatalogNameException(String name) {
        super("Ya existe un catálogo con el nombre " + name);
    }
}
