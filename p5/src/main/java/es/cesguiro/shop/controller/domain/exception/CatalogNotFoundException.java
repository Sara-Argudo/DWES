package es.cesguiro.shop.controller.domain.exception;

/**
 * Excepción del dominio: el catálogo solicitado no existe (404).
 */
public class CatalogNotFoundException extends RuntimeException {

    public CatalogNotFoundException(Long id) {
        super("No existe el catálogo " + id);
    }
}
