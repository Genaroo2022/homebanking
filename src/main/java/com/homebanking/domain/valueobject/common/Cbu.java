package com.homebanking.domain.valueobject.common;

import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object: Cbu
 */
public final class Cbu {

    private static final String CBU_REGEX = "^\\d{22}$";

    private final String value;

    private Cbu(String value) {
        this.value = value;
    }

    public static Cbu of(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidTransferDataException(DomainErrorMessages.CBU_REQUIRED);
        }
        if (!Pattern.matches(CBU_REGEX, value)) {
            throw new InvalidTransferDataException(DomainErrorMessages.CBU_ONLY_NUMBERS);
        }
        return new Cbu(value);
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
        Cbu cbu = (Cbu) o;
        return value.equals(cbu.value);
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




