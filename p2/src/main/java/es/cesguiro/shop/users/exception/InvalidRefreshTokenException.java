package es.cesguiro.shop.users.exception;

import es.cesguiro.shop.shared.exception.UnauthorizedException;

public class InvalidRefreshTokenException extends UnauthorizedException {

    public InvalidRefreshTokenException() {
        super("Invalid or expired refresh token");
    }
}
