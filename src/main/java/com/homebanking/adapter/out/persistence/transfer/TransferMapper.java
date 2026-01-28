package com.homebanking.adapter.out.persistence.transfer;

import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.valueobject.common.Cbu;
import com.homebanking.domain.valueobject.transfer.IdempotencyKey;
import com.homebanking.domain.valueobject.transfer.TransferAmount;
import com.homebanking.domain.valueobject.transfer.TransferDescription;
import org.springframework.stereotype.Component;

/**
 * Mapper: TransferMapper
 *
 * Convierte entre entidad de dominio (Transfer) y JPA entity (TransferJpaEntity).
 * Responsable de la traducción bidireccional.
 *
 * Características:
 * ✓ toJpaEntity(): UNA LÍNEA (delegación pura)
 * ✓ toDomain(): Reconstruye Transfer desde JpaEntity
 * ✓ Sin lógica de construcción (todo en factory method)
 * ✓ Únicamente mapeo de datos
 */
@Component
public class TransferMapper {

    /**
     * Convierte Transfer (domain) → TransferJpaEntity (persistence)
     *
     * SIMPLE: Solo delega al factory method de TransferJpaEntity.
     * El factory method maneja toda la lógica de construcción.
     *
     * @param transfer Transfer domain entity
     * @return TransferJpaEntity listo para persistencia
     */
    public TransferJpaEntity toJpaEntity(Transfer transfer) {
        return TransferJpaEntity.fromDomain(transfer);
    }

    /**
     * Convierte TransferJpaEntity (persistence) → Transfer (domain)
     *
     * Reconstruye la entidad de dominio desde los datos persistidos.
     * Crea todos los Value Objects a partir de valores primitivos.
     *
     * @param entity TransferJpaEntity desde la base de datos
     * @return Transfer domain entity reconstituyida
     */
    public Transfer toDomain(TransferJpaEntity entity) {
        return Transfer.reconstruct(
                entity.getId(),
                IdempotencyKey.of(entity.getIdempotencyKey()),
                entity.getOriginAccountId(),
                Cbu.of(entity.getTargetCbu()),
                TransferAmount.of(entity.getAmount()),
                TransferDescription.of(entity.getDescription()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getExecutedAt(),
                entity.getFailureReason(),
                entity.getFailedAt(),
                entity.getRetryCount(),
                entity.getLastRetryAt()
        );
    }
}