package com.homebanking.domain.exception.account;


import com.homebanking.domain.exception.common.DomainException;
public class InvalidAccountDataException extends DomainException {
    public InvalidAccountDataException(String message) {
        super(message);
    }
}



