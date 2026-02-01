package com.homebanking.domain.valueobject.card;

import com.homebanking.domain.exception.card.InvalidCardDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.util.Objects;
import java.util.regex.Pattern;

public final class CardNumber {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d{16}$");

    private final String value;

    private CardNumber(String value) {
        this.value = value;
    }

    public static CardNumber of(String value) {
        if (value == null || !NUMBER_PATTERN.matcher(value).matches() || !isLuhnValid(value)) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_NUMBER_INVALID);
        }
        return new CardNumber(value);
    }

    public String value() {
        return value;
    }

    private static boolean isLuhnValid(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CardNumber that = (CardNumber) o;
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




