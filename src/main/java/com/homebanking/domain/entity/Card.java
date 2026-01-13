package com.homebanking.domain.entity;

import com.homebanking.domain.enums.CardColor;
import com.homebanking.domain.enums.CardType;
import com.homebanking.domain.exception.InvalidCardDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d{16}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^\\d{3}$");

    private Long id;
    private Long accountId;
    private String number;
    private String cvv;
    private String cardHolder;
    private LocalDate fromDate;
    private LocalDate thruDate;
    private CardType type;
    private CardColor color;
    private boolean active;

    // To create a new card (without ID)
    public Card(Long accountId, String number, String cvv, String cardHolder,
                LocalDate fromDate, LocalDate thruDate, CardType type, CardColor color) {

        validateCardData(accountId, number, cvv, cardHolder, fromDate, thruDate, type, color);

        this.accountId = accountId;
        this.number = number;
        this.cvv = cvv;
        this.cardHolder = cardHolder.toUpperCase();
        this.fromDate = fromDate;
        this.thruDate = thruDate;
        this.type = type;
        this.color = color;
        this.active = true;
    }

    // Factory Method: Reconstitution from Persistence
    public static Card withId(Long id, Long accountId, String number, String cvv, String cardHolder,
                              LocalDate fromDate, LocalDate thruDate, CardType type, CardColor color, boolean active) {
        validateStructuralData(id);
        validateCardData(accountId, number, cvv, cardHolder, fromDate, thruDate, type, color);
        return hydrate(id, accountId, number, cvv, cardHolder, fromDate, thruDate, type, color, active);
    }
    private static Card hydrate(Long id, Long accountId, String number, String cvv, String cardHolder,
                                LocalDate fromDate, LocalDate thruDate, CardType type, CardColor color, boolean active) {
        Card card = new Card();
        card.id = id;
        card.accountId = accountId;
        card.number = number;
        card.cvv = cvv;
        card.cardHolder = cardHolder.toUpperCase();
        card.fromDate = fromDate;
        card.thruDate = thruDate;
        card.type = type;
        card.color = color;
        card.active = active;
        return card;
    }

    // --- BUSINESS METHODS ---

    public boolean isExpired() {
        return LocalDate.now().isAfter(this.thruDate);
    }

    public void activate() {
        if (this.active) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_ALREADY_ACTIVE);
        }
        if (this.isExpired()) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_EXPIRED_CANNOT_ACTIVATE);
        }
        this.active = true;
    }

    public void deactivate() {
        if (!this.active) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_ALREADY_INACTIVE);
        }
        this.active = false;
    }

    // --- VALIDATIONS (Private Static) ---

    private static void validateStructuralData(Long id) {
        if (id == null) {
            throw new InvalidCardDataException(DomainErrorMessages.ID_REQUIRED);
        }
    }

    private static void validateCardData(Long accountId, String number, String cvv, String cardHolder,
                                         LocalDate fromDate, LocalDate thruDate, CardType type, CardColor color) {
        validateAccount(accountId);
        validateNumber(number);
        validateCvv(cvv);
        validateCardHolder(cardHolder);
        validateDates(fromDate, thruDate);
    }

    private static void validateAccount(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_ACCOUNT_REQUIRED);
        }
    }

    private static void validateNumber(String number) {
        if (number == null || !NUMBER_PATTERN.matcher(number).matches() || !isLuhnValid(number)) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_NUMBER_INVALID);
        }
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

    private static void validateCvv(String cvv) {
        if (cvv == null || !CVV_PATTERN.matcher(cvv).matches()) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_CVV_INVALID);
        }
    }

    private static void validateCardHolder(String cardHolder) {
        if (cardHolder == null || cardHolder.isBlank()) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_HOLDER_REQUIRED);
        }
    }

    private static void validateDates(LocalDate from, LocalDate thru) {
        if (from == null || thru == null) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_DATES_REQUIRED);
        }
        if (thru.isBefore(from)) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_INVALID_DATES);
        }
        if (thru.isBefore(LocalDate.now())) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_EXPIRED);
        }
    }
}