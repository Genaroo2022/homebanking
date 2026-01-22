
package com.homebanking.adapter.out.persistence.transfer;

import com.homebanking.domain.entity.Transfer;
import org.springframework.stereotype.Component;

/**
 * Mapper: TransferMapper

 * Convierte entre entidad de dominio (Transfer) y JPA entity (TransferJpaEntity).
 * Responsable de la traducción bidireccional.
 */
@Component
class TransferMapper {

    public TransferJpaEntity toJpaEntity(Transfer transfer) {
        TransferJpaEntity entity = new TransferJpaEntity(
                transfer.getIdempotencyKey(),
                transfer.getOriginAccountId(),
                transfer.getTargetCbu(),
                transfer.getAmount(),
                transfer.getDescription(),
                transfer.getStatus(),
                transfer.getCreatedAt()
        );

        if (transfer.getId() != null) {
            entity.setId(transfer.getId());
        }

        // Campos opcionales que pueden existir si la transferencia fue procesada
        if (transfer.getExecutedAt() != null) {
            entity.executedAt = transfer.getExecutedAt();
        }
        if (transfer.getFailedAt() != null) {
            entity.failedAt = transfer.getFailedAt();
        }
        if (transfer.getFailureReason() != null) {
            entity.failureReason = transfer.getFailureReason();
        }
        if (transfer.getRetryCount() != null) {
            entity.retryCount = transfer.getRetryCount();
        }
        if (transfer.getLastRetryAt() != null) {
            entity.lastRetryAt = transfer.getLastRetryAt();
        }

        return entity;
    }

    public Transfer toDomain(TransferJpaEntity entity) {
        return Transfer.withId(
                entity.getId(),
                entity.getIdempotencyKey(),
                entity.getOriginAccountId(),
                entity.getTargetCbu(),
                entity.getAmount(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getExecutedAt(),
                entity.getFailedAt(),
                entity.getFailureReason(),
                entity.getRetryCount(),
                entity.getLastRetryAt()
        );
    }
}
