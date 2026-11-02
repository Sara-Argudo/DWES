package es.cesguiro.shop.catalog.exception;

import es.cesguiro.shop.shared.exception.ConflictException;

public class InsufficientStockException extends ConflictException {

    public InsufficientStockException(long productId, int requested) {
        super("Insufficient stock for product " + productId + " (requested " + requested + ")");
    }
}
