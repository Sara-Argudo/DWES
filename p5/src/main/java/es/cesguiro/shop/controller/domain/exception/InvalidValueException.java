package es.cesguiro.shop.controller.domain.exception;

/**
 * Excepción del dominio: un valor propone un estado imposible para el negocio.
 * Los value objects (Money y Stock) la lanzan en su construcción;
 * la presentación la mapea al error de formato del contrato (400).
 */
public class InvalidValueException extends RuntimeException {

    public InvalidValueException(String message) {
        super(message);
    }
}
