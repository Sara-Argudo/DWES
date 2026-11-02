package es.cesguiro.shop.shared.exception;

/**
 * Recurso inexistente: se mapea a 404 Not Found.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String detail) {
        super(detail);
    }
}
