package es.cesguiro.shop.shared.exception;

/**
 * Credenciales o token inválidos: se mapea a 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String detail) {
        super(detail);
    }
}
