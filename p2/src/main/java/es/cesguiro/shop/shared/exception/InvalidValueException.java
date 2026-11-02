package es.cesguiro.shop.shared.exception;

/**
 * Valor fuera del rango o la forma que exige el dominio: se mapea a 400 Bad Request.
 */
public class InvalidValueException extends RuntimeException {

    public InvalidValueException(String detail) {
        super(detail);
    }
}
