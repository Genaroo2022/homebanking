package com.homebanking.domain.exception.payment;

import com.homebanking.domain.exception.common.DomainException;

import java.util.UUID;

public class BillPaymentNotFoundException extends DomainException {

    private final UUID paymentId;

    public BillPaymentNotFoundException(String message, UUID paymentId) {
        super(message);
        this.paymentId = paymentId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }
}

