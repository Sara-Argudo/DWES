package es.cesguiro.shop.controller.domain.exception;

/**
 * Excepción del dominio: el producto solicitado no existe (404).
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("No existe el producto " + id);
    }
}
