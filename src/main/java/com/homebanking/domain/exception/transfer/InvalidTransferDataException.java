package com.homebanking.domain.exception.transfer;


import com.homebanking.domain.exception.common.DomainException;
public class InvalidTransferDataException extends DomainException {
    public InvalidTransferDataException(String message) {
        super(message);
    }
}



