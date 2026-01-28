package com.homebanking.domain.valueobject.transfer;

import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.util.Objects;

/**
 * Value Object: IdempotencyKey
 */
public final class IdempotencyKey {

    private final String value;

    private IdempotencyKey(String value) {
        this.value = value;
    }

    public static IdempotencyKey of(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidTransferDataException(DomainErrorMessages.IDEMPOTENCY_KEY_REQUIRED);
        }
        return new IdempotencyKey(value);
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
        IdempotencyKey that = (IdempotencyKey) o;
        return value.equals(that.value);
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


