package com.homebanking.domain.exception;

public class InvalidTransferDataException extends DomainException {
    public InvalidTransferDataException(String message) {
        super(message);
    }
}