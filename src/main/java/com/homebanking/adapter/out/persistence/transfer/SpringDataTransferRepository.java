package com.homebanking.adapter.out.persistence.transfer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.homebanking.domain.enums.TransferStatus;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data Repository: SpringDataTransferRepository

 * Manejo de persistencia de bajo nivel.
 * Queries optimizadas con índices para consultas frecuentes.
 */
@Repository
interface SpringDataTransferRepository extends JpaRepository<TransferJpaEntity, Long> {

    /**
     * Busca transferencia por idempotency key (CRÍTICO para idempotencia).
     */
    Optional<TransferJpaEntity> findByIdempotencyKey(String idempotencyKey);

    /**
     * Lista transferencias de una cuenta origen.
     */
    List<TransferJpaEntity> findByOriginAccountId(Long originAccountId);

    /**
     * Obtiene transferencias pendientes para procesamiento asincrónico.
     */
    @Query("SELECT t FROM TransferJpaEntity t WHERE t.status = 'PENDING' ORDER BY t.createdAt ASC")
    List<TransferJpaEntity> findPendingTransfers();

    /**
     * Obtiene transferencias fallidas que pueden ser reintentadas.
     * Limita a 100 registros para evitar sobrecargar el servicio de reintentos.
     */
    @Query("SELECT t FROM TransferJpaEntity t " +
            "WHERE t.status = 'FAILED' AND t.retryCount < 3 " +
            "ORDER BY t.lastRetryAt ASC LIMIT 100")
    List<TransferJpaEntity> findRetryableTransfers();
}