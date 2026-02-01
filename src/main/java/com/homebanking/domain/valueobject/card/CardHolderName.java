package com.homebanking.domain.valueobject.card;

import com.homebanking.domain.exception.card.InvalidCardDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.util.Objects;

public final class CardHolderName {

    private final String value;

    private CardHolderName(String value) {
        this.value = value;
    }

    public static CardHolderName of(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_HOLDER_REQUIRED);
        }
        return new CardHolderName(value.toUpperCase());
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
        CardHolderName that = (CardHolderName) o;
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




