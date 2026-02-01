
/*
 * Excepción: AccountNotFoundException
 *
 * Lanzada cuando no se encuentra una cuenta requerida.
 */
package com.homebanking.domain.exception.account;

import com.homebanking.domain.exception.common.DomainException;

import java.util.UUID;

public class AccountNotFoundException extends DomainException {

    private final UUID accountId;

    public AccountNotFoundException(String message, UUID accountId) {
        super(message);
        this.accountId = accountId;
    }

    public UUID getAccountId() {
        return accountId;
    }
}



