package com.homebanking.domain.entity;

import com.homebanking.domain.enums.CardColor;
import com.homebanking.domain.enums.CardType;
import com.homebanking.domain.exception.card.InvalidCardDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.domain.valueobject.card.CardCvv;
import com.homebanking.domain.valueobject.card.CardHolderName;
import com.homebanking.domain.valueobject.card.CardNumber;
import com.homebanking.domain.valueobject.card.CardValidity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card {

    private UUID id;
    private UUID accountId;
    private CardNumber number;
    private CardCvv cvv;
    private CardHolderName cardHolder;
    private CardValidity validity;
    private CardType type;
    private CardColor color;
    private boolean active;

    // To create a new card (without ID)
    public Card(UUID accountId, String number, String cvv, String cardHolder,
                LocalDate fromDate, LocalDate thruDate, CardType type, CardColor color) {

        validateCardData(accountId, number, cvv, cardHolder, fromDate, thruDate, type, color);

        this.accountId = accountId;
        this.number = CardNumber.of(number);
        this.cvv = CardCvv.of(cvv);
        this.cardHolder = CardHolderName.of(cardHolder);
        this.validity = CardValidity.of(fromDate, thruDate);
        this.type = type;
        this.color = color;
        this.active = true;
    }

    // Factory Method: Reconstitution from Persistence
    public static Card withId(UUID id, UUID accountId, String number, String cvv, String cardHolder,
                              LocalDate fromDate, LocalDate thruDate, CardType type, CardColor color, boolean active) {
        validateStructuralData(id);
        validateCardData(accountId, number, cvv, cardHolder, fromDate, thruDate, type, color);
        return hydrate(id, accountId, CardNumber.of(number), CardCvv.of(cvv), CardHolderName.of(cardHolder),
                CardValidity.of(fromDate, thruDate), type, color, active);
    }
    private static Card hydrate(UUID id, UUID accountId, CardNumber number, CardCvv cvv, CardHolderName cardHolder,
                                CardValidity validity, CardType type, CardColor color, boolean active) {
        Card card = new Card();
        card.id = id;
        card.accountId = accountId;
        card.number = number;
        card.cvv = cvv;
        card.cardHolder = cardHolder;
        card.validity = validity;
        card.type = type;
        card.color = color;
        card.active = active;
        return card;
    }

    // --- BUSINESS METHODS ---

    public boolean isExpired() {
        return this.validity.isExpired();
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

    private static void validateStructuralData(UUID id) {
        if (id == null) {
            throw new InvalidCardDataException(DomainErrorMessages.ID_REQUIRED);
        }
    }

    private static void validateCardData(UUID accountId, String number, String cvv, String cardHolder,
                                         LocalDate fromDate, LocalDate thruDate, CardType type, CardColor color) {
        validateAccount(accountId);
    }

    private static void validateAccount(UUID accountId) {
        if (accountId == null) {
            throw new InvalidCardDataException(DomainErrorMessages.CARD_ACCOUNT_REQUIRED);
        }
    }
}

