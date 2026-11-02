package es.cesguiro.shop.catalog.exception;

import es.cesguiro.shop.shared.exception.ConflictException;

public class CatalogNotEmptyException extends ConflictException {

    public CatalogNotEmptyException(long catalogId) {
        super("Catalog " + catalogId + " has products and cannot be deleted");
    }
}
