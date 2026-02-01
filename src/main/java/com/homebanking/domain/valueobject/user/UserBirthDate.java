package com.homebanking.domain.valueobject.user;

import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

public final class UserBirthDate {

    private static final int MIN_LEGAL_AGE = 18;
    private static final int MAX_LEGAL_AGE = 130;

    private final LocalDate value;

    private UserBirthDate(LocalDate value) {
        this.value = value;
    }

    public static UserBirthDate of(LocalDate value) {
        if (value == null) {
            throw new InvalidUserDataException(DomainErrorMessages.BIRTHDATE_REQUIRED);
        }
        int years = Period.between(value, LocalDate.now()).getYears();
        if (years < MIN_LEGAL_AGE) {
            throw new InvalidUserDataException(DomainErrorMessages.USER_UNDERAGE);
        }
        if (years > MAX_LEGAL_AGE) {
            throw new InvalidUserDataException(DomainErrorMessages.USER_OVER_MAX_AGE);
        }
        return new UserBirthDate(value);
    }

    public LocalDate value() {
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
        UserBirthDate that = (UserBirthDate) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}




