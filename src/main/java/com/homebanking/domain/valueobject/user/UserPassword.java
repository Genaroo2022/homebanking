package com.homebanking.domain.valueobject.user;

import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.util.Objects;

public final class UserPassword {

    private static final int MIN_LENGTH = 8;

    private final String value;

    private UserPassword(String value) {
        this.value = value;
    }

    public static UserPassword of(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserDataException(DomainErrorMessages.MANDATORY_FIELDS);
        }
        if (value.length() < MIN_LENGTH) {
            throw new InvalidUserDataException(DomainErrorMessages.PASSWORD_FORMAT);
        }
        return new UserPassword(value);
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
        UserPassword that = (UserPassword) o;
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




