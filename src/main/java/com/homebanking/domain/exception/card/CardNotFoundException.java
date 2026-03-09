package com.homebanking.domain.exception.card;

import com.homebanking.domain.exception.common.DomainException;

import java.util.UUID;

public class CardNotFoundException extends DomainException {

    private final UUID cardId;

    public CardNotFoundException(String message, UUID cardId) {
        super(message);
        this.cardId = cardId;
    }

    public UUID getCardId() {
        return cardId;
    }
}

