package com.homebanking.domain.exception.user;

import com.homebanking.domain.exception.common.DomainException;

public class TooManyLoginAttemptsException extends DomainException {

    private final long retryAfterSeconds;

    public TooManyLoginAttemptsException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
