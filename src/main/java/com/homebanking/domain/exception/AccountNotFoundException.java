
/*
 * Excepción: AccountNotFoundException
 *
 * Lanzada cuando no se encuentra una cuenta requerida.
 */
package com.homebanking.domain.exception;

public class AccountNotFoundException extends DomainException {

    private final Long accountId;

    public AccountNotFoundException(String message, Long accountId) {
        super(message);
        this.accountId = accountId;
    }

    public Long getAccountId() {
        return accountId;
    }
}