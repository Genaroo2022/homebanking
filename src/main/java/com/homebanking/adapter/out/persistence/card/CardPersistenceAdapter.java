package com.homebanking.adapter.out.persistence.card;

import com.homebanking.domain.entity.Card;
import com.homebanking.port.out.card.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class CardPersistenceAdapter implements CardRepository {

    private final SpringDataCardRepository repository;
    private final CardPersistenceMapper mapper;

    @Override
    public Card save(Card card) {
        CardJpaEntity saved = repository.save(mapper.toJpa(card));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Card> findById(UUID cardId) {
        return repository.findById(cardId).map(mapper::toDomain);
    }

    @Override
    public List<Card> findByAccountId(UUID accountId) {
        return repository.findByAccountId(accountId).stream().map(mapper::toDomain).toList();
    }
}

