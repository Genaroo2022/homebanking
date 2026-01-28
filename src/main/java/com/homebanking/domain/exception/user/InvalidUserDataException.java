package com.homebanking.domain.exception.user;


import com.homebanking.domain.exception.common.DomainException;
public class InvalidUserDataException extends DomainException {
    public InvalidUserDataException(String message) {
        super(message);
    }
}

