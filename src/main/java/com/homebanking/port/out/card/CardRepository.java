package com.homebanking.port.out.card;

import com.homebanking.domain.entity.Card;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository {
    Card save(Card card);
    Optional<Card> findById(UUID cardId);
    List<Card> findByAccountId(UUID accountId);
}

