package com.homebanking.domain.exception.account;



import com.homebanking.domain.exception.common.DomainException;

import java.util.UUID;

/**
 * Excepción: InsufficientFundsException

 * Lanzada cuando se intenta transferir más dinero del disponible.
 * Es una excepción de NEGOCIO, no técnica.
 */
public class InsufficientFundsException extends DomainException {

    private final UUID accountId;
    private final java.math.BigDecimal requestedAmount;
    private final java.math.BigDecimal availableBalance;

    public InsufficientFundsException(
            String message,
            UUID accountId,
            java.math.BigDecimal requestedAmount,
            java.math.BigDecimal availableBalance) {
        super(message);
        this.accountId = accountId;
        this.requestedAmount = requestedAmount;
        this.availableBalance = availableBalance;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public java.math.BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public java.math.BigDecimal getAvailableBalance() {
        return availableBalance;
    }
}


