package com.homebanking.domain.valueobject.account;

import com.homebanking.domain.exception.account.InvalidAccountDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.util.Objects;
import java.util.regex.Pattern;

public final class AccountAlias {

    private static final String ALIAS_REGEX = "^[a-zA-Z0-9.]{6,20}$";

    private final String value;

    private AccountAlias(String value) {
        this.value = value;
    }

    public static AccountAlias of(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidAccountDataException(DomainErrorMessages.ALIAS_REQUIRED);
        }
        if (!Pattern.matches(ALIAS_REGEX, value)) {
            throw new InvalidAccountDataException(DomainErrorMessages.ALIAS_INVALID_FORMAT);
        }
        return new AccountAlias(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountAlias accountAlias = (AccountAlias) o;
        return value.equals(accountAlias.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}


