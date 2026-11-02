package es.cesguiro.shop.shared.exception;

/**
 * Regla de negocio incumplida con la petición: se mapea a 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String detail) {
        super(detail);
    }
}
