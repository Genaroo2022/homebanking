package com.homebanking.domain.exception;

public class TransferAlreadyCompletedException extends DomainException {
    public TransferAlreadyCompletedException(String message) {
        super(message);
    }
}
