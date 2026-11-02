package es.cesguiro.shop.catalog.exception;

import es.cesguiro.shop.shared.exception.ConflictException;

public class DuplicateCatalogNameException extends ConflictException {

    public DuplicateCatalogNameException(String name) {
        super("Catalog name " + name + " already exists");
    }
}
