package com.homebanking.domain.valueobject.user;

import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.util.Objects;
import java.util.regex.Pattern;

public final class UserDni {

    private static final int MIN_LENGTH = 7;
    private static final int MAX_LENGTH = 20;
    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d+$");

    private final String value;

    private UserDni(String value) {
        this.value = value;
    }

    public static UserDni of(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserDataException(DomainErrorMessages.MANDATORY_FIELDS);
        }
        if (value.length() < MIN_LENGTH) {
            throw new InvalidUserDataException(DomainErrorMessages.DNI_INVALID);
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidUserDataException(DomainErrorMessages.DNI_TOO_LONG);
        }
        if (!DNI_PATTERN.matcher(value).matches()) {
            throw new InvalidUserDataException(DomainErrorMessages.INVALID_DNI_FORMAT);
        }
        return new UserDni(value);
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
        UserDni userDni = (UserDni) o;
        return value.equals(userDni.value);
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




