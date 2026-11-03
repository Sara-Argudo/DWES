package es.cesguiro.shop.controller;

import es.cesguiro.shop.api.model.Problem;
import es.cesguiro.shop.domain.CatalogNotFoundException;
import es.cesguiro.shop.domain.CatalogNotEmptyException;
import es.cesguiro.shop.domain.DuplicateCatalogNameException;
import es.cesguiro.shop.domain.ProductNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Convierte las excepciones del dominio en las respuestas Problem (RFC 9457)
 * que declara el contrato: `type`, `title`, `status` y `detail`.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    private static final String ERROR_TYPE_BASE = "https://cesguiro.es/shop/errors/";

    @ExceptionHandler(CatalogNotFoundException.class)
    public ResponseEntity<Problem> handleCatalogNotFound(CatalogNotFoundException exception) {
        return problem(exception, 404, "catalog-not-found", "Catálogo no existe");
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Problem> handleProductNotFound(ProductNotFoundException exception) {
        return problem(exception, 404, "product-not-found", "Producto no existe");
    }

    @ExceptionHandler(DuplicateCatalogNameException.class)
    public ResponseEntity<Problem> handleDuplicateCatalogName(DuplicateCatalogNameException exception) {
        return problem(exception, 409, "duplicate-catalog-name", "Nombre de catálogo repetido");
    }

    @ExceptionHandler(CatalogNotEmptyException.class)
    public ResponseEntity<Problem> handleCatalogNotEmpty(CatalogNotEmptyException exception) {
        return problem(exception, 409, "catalog-not-empty", "Catálogo con productos");
    }

    private ResponseEntity<Problem> problem(
            RuntimeException exception, int status, String type, String title) {
        Problem problem = new Problem(
                ERROR_TYPE_BASE + type,
                title,
                status,
                exception.getMessage()
        );
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
