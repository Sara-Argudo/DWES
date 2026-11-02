package es.cesguiro.shop.catalog.exception;

import es.cesguiro.shop.shared.exception.NotFoundException;

public class CatalogNotFoundException extends NotFoundException {

    public CatalogNotFoundException(long catalogId) {
        super("Catalog " + catalogId + " does not exist");
    }
}
