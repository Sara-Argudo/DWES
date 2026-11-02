package es.cesguiro.shop.users.exception;

import es.cesguiro.shop.shared.exception.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
