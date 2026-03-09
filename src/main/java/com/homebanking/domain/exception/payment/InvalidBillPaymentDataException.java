package com.homebanking.domain.exception.payment;

import com.homebanking.domain.exception.common.DomainException;

public class InvalidBillPaymentDataException extends DomainException {
    public InvalidBillPaymentDataException(String message) {
        super(message);
    }
}

