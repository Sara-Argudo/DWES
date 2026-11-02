package es.cesguiro.shop.users.exception;

import es.cesguiro.shop.shared.exception.ConflictException;

public class DuplicateEmailException extends ConflictException {

    public DuplicateEmailException(String email) {
        super("Email " + email + " is already registered");
    }
}
