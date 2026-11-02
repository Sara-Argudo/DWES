package es.cesguiro.shop.catalog.exception;

import es.cesguiro.shop.shared.exception.NotFoundException;

public class ProductNotFoundException extends NotFoundException {

    public ProductNotFoundException(long productId) {
        super("Product " + productId + " does not exist");
    }
}
