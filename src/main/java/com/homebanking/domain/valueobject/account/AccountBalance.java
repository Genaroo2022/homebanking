package com.homebanking.domain.valueobject.account;

import com.homebanking.domain.exception.account.InvalidAccountDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.math.BigDecimal;
import java.util.Objects;

public final class AccountBalance {

    private final BigDecimal value;

    private AccountBalance(BigDecimal value) {
        this.value = value;
    }

    public static AccountBalance of(BigDecimal value) {
        if (value == null) {
            throw new InvalidAccountDataException(DomainErrorMessages.BALANCE_REQUIRED);
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAccountDataException(DomainErrorMessages.ACCOUNT_BALANCE_NEGATIVE);
        }
        return new AccountBalance(value);
    }

    public BigDecimal value() {
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
        AccountBalance that = (AccountBalance) o;
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}


