package com.homebanking.domain.valueobject.card;

import com.homebanking.domain.exception.card.InvalidCardDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.util.Objects;
import java.util.regex.Pattern;

public final class CardCvv {

    private static final Pattern CVV_PATTERN = Pattern.compile("^\\d{3}$");

    private final String value;

    private CardCvv(String value) {
        this.value = value;
    }

    public static CardCvv of(String value) {
        if (value == null || !CVV_PATTERN.matcher(value).matches()) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_CVV_INVALID);
        }
        return new CardCvv(value);
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
        CardCvv cardCvv = (CardCvv) o;
        return value.equals(cardCvv.value);
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




