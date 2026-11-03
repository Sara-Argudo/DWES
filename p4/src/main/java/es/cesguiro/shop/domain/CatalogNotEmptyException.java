package es.cesguiro.shop.domain;

/**
 * Excepción del dominio: el catálogo todavía tiene productos y no se puede borrar (409).
 */
public class CatalogNotEmptyException extends RuntimeException {

    public CatalogNotEmptyException(Long id, long productCount) {
        super("El catálogo " + id + " tiene " + productCount + " productos y no se puede borrar");
    }
}
