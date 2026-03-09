package com.homebanking.application.mapper;

import com.homebanking.application.dto.card.response.CardOutputResponse;
import com.homebanking.domain.entity.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public CardOutputResponse toDto(Card card) {
        String number = card.getNumber().value();
        String masked = "**** **** **** " + number.substring(number.length() - 4);
        return new CardOutputResponse(
                card.getId(),
                card.getAccountId(),
                masked,
                card.getCardHolder().value(),
                card.getValidity().fromDate().toString(),
                card.getValidity().thruDate().toString(),
                card.getType().name(),
                card.getColor().name(),
                card.isActive()
        );
    }
}
