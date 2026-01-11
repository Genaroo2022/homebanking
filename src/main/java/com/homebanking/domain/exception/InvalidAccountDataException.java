package com.homebanking.domain.exception;

public class InvalidAccountDataException extends DomainException {
    public InvalidAccountDataException(String message) {
        super(message);
    }
}