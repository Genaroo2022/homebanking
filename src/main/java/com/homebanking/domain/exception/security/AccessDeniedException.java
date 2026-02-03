package com.homebanking.domain.exception.security;

import com.homebanking.domain.exception.common.DomainException;

public class AccessDeniedException extends DomainException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
