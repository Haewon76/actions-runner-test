package com.cashmallow.api.domain.shared;

public class InvalidPasswordException extends CashmallowException {

    public InvalidPasswordException(String message) {
        super(message);
    }

    public InvalidPasswordException(String message, Exception e) {
        super(message, e);
    }

    public InvalidPasswordException(String message, String option) {
        super(message, option);
    }
}
