package com.homebanking.adapter.out.persistence.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataBillPaymentRepository extends JpaRepository<BillPaymentJpaEntity, UUID> {
    Optional<BillPaymentJpaEntity> findByIdempotencyKey(String idempotencyKey);
}

