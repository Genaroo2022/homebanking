package com.homebanking.domain.exception;


/**
 * Excepción: InsufficientFundsException

 * Lanzada cuando se intenta transferir más dinero del disponible.
 * Es una excepción de NEGOCIO, no técnica.
 */
public class InsufficientFundsException extends DomainException {

    private final Long accountId;
    private final java.math.BigDecimal requestedAmount;
    private final java.math.BigDecimal availableBalance;

    public InsufficientFundsException(
            String message,
            Long accountId,
            java.math.BigDecimal requestedAmount,
            java.math.BigDecimal availableBalance) {
        super(message);
        this.accountId = accountId;
        this.requestedAmount = requestedAmount;
        this.availableBalance = availableBalance;
    }

    public Long getAccountId() {
        return accountId;
    }

    public java.math.BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public java.math.BigDecimal getAvailableBalance() {
        return availableBalance;
    }
}
