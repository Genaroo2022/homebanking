package com.homebanking.domain.exception.transfer;


import com.homebanking.domain.exception.common.DomainException;
public class TransferAlreadyCompletedException extends DomainException {
    public TransferAlreadyCompletedException(String message) {
        super(message);
    }
}




