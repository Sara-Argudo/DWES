package es.cesguiro.shop.shared.web;

import es.cesguiro.shop.catalog.exception.CatalogNotEmptyException;
import es.cesguiro.shop.catalog.exception.DuplicateCatalogNameException;
import es.cesguiro.shop.catalog.exception.InsufficientStockException;
import es.cesguiro.shop.orders.exception.OrderAlreadyCancelledException;
import es.cesguiro.shop.shared.exception.ConflictException;
import es.cesguiro.shop.shared.exception.InvalidValueException;
import es.cesguiro.shop.shared.exception.NotFoundException;
import es.cesguiro.shop.shared.exception.UnauthorizedException;
import es.cesguiro.shop.users.exception.DuplicateEmailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones del dominio a respuestas Problem (RFC 9457),
 * con títulos en inglés según la convención del enunciado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Object> problem(HttpStatus status, String title, String detail) {
        var problem = org.springframework.http.ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> notFound(NotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Not Found", e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Object> conflict(ConflictException e) {
        return problem(HttpStatus.CONFLICT, "Conflict", e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> unauthorized(UnauthorizedException e) {
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", e.getMessage());
    }

    @ExceptionHandler(InvalidValueException.class)
    public ResponseEntity<Object> invalidValue(InvalidValueException e) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", e.getMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Object> duplicateEmail(DuplicateEmailException e) {
        return problem(HttpStatus.CONFLICT, "Conflict", e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> forbidden(AccessDeniedException e) {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", "Access denied");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> invalidBody(MethodArgumentNotValidException e) {
        var detail = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Invalid request");
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> unreadable(HttpMessageNotReadableException e) {
        var logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);
        logger.warn("Malformed request body", e);
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", "Malformed request body");
    }
}
