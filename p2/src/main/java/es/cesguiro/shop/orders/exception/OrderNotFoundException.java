package es.cesguiro.shop.orders.exception;

import es.cesguiro.shop.shared.exception.NotFoundException;

public class OrderNotFoundException extends NotFoundException {

    public OrderNotFoundException(long orderId) {
        super("Order " + orderId + " does not exist");
    }
}
