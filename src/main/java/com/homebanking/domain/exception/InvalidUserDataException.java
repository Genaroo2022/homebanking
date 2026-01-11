package com.homebanking.domain.exception;

public class InvalidUserDataException extends DomainException {
    public InvalidUserDataException(String message) {
        super(message);
    }
}