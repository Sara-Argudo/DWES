package es.cesguiro.shop.orders.exception;

import es.cesguiro.shop.shared.exception.ConflictException;

public class OrderAlreadyCancelledException extends ConflictException {

    public OrderAlreadyCancelledException(long orderId) {
        super("Order " + orderId + " is already cancelled");
    }
}
