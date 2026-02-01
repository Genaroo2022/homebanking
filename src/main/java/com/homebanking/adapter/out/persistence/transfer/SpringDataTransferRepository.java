package com.homebanking.adapter.out.persistence.transfer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.homebanking.domain.enums.TransferStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data Repository: SpringDataTransferRepository

 * Manejo de persistencia de bajo nivel.
 * Queries optimizadas con índices para consultas frecuentes.
 */
@Repository
interface SpringDataTransferRepository extends JpaRepository<TransferJpaEntity, UUID> {

    /**
     * Busca transferencia por idempotency key (CRÍTICO para idempotencia).
     */
    Optional<TransferJpaEntity> findByIdempotencyKey(String idempotencyKey);

    /**
     * Lista transferencias de una cuenta origen.
     */
    List<TransferJpaEntity> findByOriginAccountId(UUID originAccountId);

    /**
     * Obtiene transferencias pendientes para procesamiento asincrónico.
     */
    @Query("SELECT t FROM TransferJpaEntity t WHERE t.status = 'PENDING' ORDER BY t.createdAt ASC")
    List<TransferJpaEntity> findPendingTransfers();

    /**
     * Obtiene transferencias fallidas que pueden ser reintentadas.
     * Limita a 100 registros para evitar sobrecargar el servicio de reintentos.
     */
    List<TransferJpaEntity> findTop100ByStatusAndRetryCountLessThanOrderByLastRetryAtAsc(
            TransferStatus status,
            Integer retryCount);
}
