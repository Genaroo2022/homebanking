package com.homebanking.domain.valueobject.user;

import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.util.Objects;
import java.util.regex.Pattern;

public final class UserLastName {

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-Z\\u00C0-\\u00FF\\u00F1\\u00D1\\s]+$");

    private final String value;

    private UserLastName(String value) {
        this.value = value;
    }

    public static UserLastName of(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserDataException(DomainErrorMessages.MANDATORY_FIELDS);
        }
        if (!NAME_PATTERN.matcher(value).matches()) {
            throw new InvalidUserDataException(DomainErrorMessages.INVALID_NAME_FORMAT);
        }
        return new UserLastName(value);
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
        UserLastName that = (UserLastName) o;
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


