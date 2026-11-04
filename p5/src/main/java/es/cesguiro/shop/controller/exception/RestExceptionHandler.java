package es.cesguiro.shop.controller.exception;

import es.cesguiro.shop.controller.domain.exception.CatalogNotEmptyException;
import es.cesguiro.shop.controller.domain.exception.CatalogNotFoundException;
import es.cesguiro.shop.controller.domain.exception.DuplicateCatalogNameException;
import es.cesguiro.shop.controller.domain.exception.InvalidValueException;
import es.cesguiro.shop.controller.domain.exception.ProductNotFoundException;
import es.cesguiro.shop.controller.dto.Problem;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Convierte las excepciones en las respuestas Problem (RFC 9457) que declara
 * el contrato: `type`, `title`, `status` y `detail`.
 *
 * La petición de entrada que no cumple los constraints del contrato responde
 * 400 con campo y motivo por cada violación (excepciones de Jakarta Validation);
 * las excepciones del dominio se mapean a sus estados: 400 para la invariante
 * que algún código permita violar, 404 para lo inexistente y 409 para el
 * estado incompatible con la operación.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    private static final String ERROR_TYPE_BASE = "https://cesguiro.es/shop/errors/";

    /** 400: la petición no cumple los constraints del contrato (formato). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleValidation(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(RestExceptionHandler::violationDetail)
                .reduce((a, b) -> a + "; " + b)
                .orElse("Invalid request");
        Problem problem = new Problem(
                ERROR_TYPE_BASE + "validation-failed",
                "Petición no válida",
                400,
                detail
        );
        return ResponseEntity.status(400)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    /** 400: la invariante de los value objects, violada por cualquier otro flujo. */
    @ExceptionHandler(InvalidValueException.class)
    public ResponseEntity<Problem> handleInvalidValue(InvalidValueException exception) {
        return problem(exception, 400, "invalid-value", "Valor no válido");
    }

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

    /** Campo y motivo de cada violación, para el detalle del 400. */
    private static String violationDetail(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
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
