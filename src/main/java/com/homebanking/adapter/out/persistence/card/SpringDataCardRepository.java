package com.homebanking.adapter.out.persistence.card;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataCardRepository extends JpaRepository<CardJpaEntity, UUID> {
    List<CardJpaEntity> findByAccountId(UUID accountId);
}

