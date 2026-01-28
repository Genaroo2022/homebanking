package com.homebanking.domain.valueobject.card;

import com.homebanking.domain.exception.card.InvalidCardDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.time.LocalDate;
import java.util.Objects;

public final class CardValidity {

    private final LocalDate fromDate;
    private final LocalDate thruDate;

    private CardValidity(LocalDate fromDate, LocalDate thruDate) {
        this.fromDate = fromDate;
        this.thruDate = thruDate;
    }

    public static CardValidity of(LocalDate fromDate, LocalDate thruDate) {
        if (fromDate == null || thruDate == null) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_DATES_REQUIRED);
        }
        if (thruDate.isBefore(fromDate)) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_INVALID_DATES);
        }
        if (thruDate.isBefore(LocalDate.now())) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_EXPIRED);
        }
        return new CardValidity(fromDate, thruDate);
    }

    public LocalDate fromDate() {
        return fromDate;
    }

    public LocalDate thruDate() {
        return thruDate;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(thruDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CardValidity that = (CardValidity) o;
        return fromDate.equals(that.fromDate) && thruDate.equals(that.thruDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromDate, thruDate);
    }

    @Override
    public String toString() {
        return fromDate + " - " + thruDate;
    }
}


