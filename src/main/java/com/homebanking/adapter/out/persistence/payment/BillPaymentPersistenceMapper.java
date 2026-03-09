package com.homebanking.adapter.out.persistence.payment;

import com.homebanking.domain.entity.BillPayment;
import org.springframework.stereotype.Component;

@Component
public class BillPaymentPersistenceMapper {

    public BillPaymentJpaEntity toJpa(BillPayment payment) {
        return BillPaymentJpaEntity.of(
                payment.getId(),
                payment.getAccountId(),
                payment.getBillerCode(),
                payment.getReference(),
                payment.getAmount(),
                payment.getIdempotencyKey(),
                payment.getStatus(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getProcessedAt(),
                0L
        );
    }

    public BillPayment toDomain(BillPaymentJpaEntity entity) {
        return BillPayment.reconstruct(
                entity.getId(),
                entity.getAccountId(),
                entity.getBillerCode(),
                entity.getReference(),
                entity.getAmount(),
                entity.getIdempotencyKey(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getProcessedAt()
        );
    }
}

