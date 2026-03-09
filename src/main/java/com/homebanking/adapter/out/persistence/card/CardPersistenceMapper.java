package com.homebanking.adapter.out.persistence.card;

import com.homebanking.domain.entity.Card;
import com.homebanking.port.out.security.CardDataProtector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardPersistenceMapper {

    private final CardDataProtector cardDataProtector;

    public CardJpaEntity toJpa(Card card) {
        String cardNumber = card.getNumber().value();
        return CardJpaEntity.of(
                card.getId(),
                card.getAccountId(),
                cardDataProtector.encrypt(cardNumber),
                cardDataProtector.encrypt(card.getCvv().value()),
                cardNumber.substring(cardNumber.length() - 4),
                card.getCardHolder().value(),
                card.getValidity().fromDate(),
                card.getValidity().thruDate(),
                card.getType(),
                card.getColor(),
                card.isActive(),
                0L
        );
    }

    public Card toDomain(CardJpaEntity entity) {
        return Card.withId(
                entity.getId(),
                entity.getAccountId(),
                cardDataProtector.decrypt(entity.getEncryptedNumber()),
                cardDataProtector.decrypt(entity.getEncryptedCvv()),
                entity.getCardHolder(),
                entity.getFromDate(),
                entity.getThruDate(),
                entity.getType(),
                entity.getColor(),
                entity.isActive()
        );
    }
}
