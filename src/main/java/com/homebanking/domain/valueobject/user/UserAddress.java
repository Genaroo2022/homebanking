package com.homebanking.domain.valueobject.user;

import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.util.Objects;

public final class UserAddress {

    private final String value;

    private UserAddress(String value) {
        this.value = value;
    }

    public static UserAddress of(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserDataException(DomainErrorMessages.MANDATORY_FIELDS);
        }
        return new UserAddress(value);
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
        UserAddress that = (UserAddress) o;
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




