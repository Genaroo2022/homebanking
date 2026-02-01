
/*
 * Output Port: TransferRepository

 * Contrato para persistencia de transferencias.
 * Implementado por adapters de persistencia.
 */
package com.homebanking.port.out.transfer;

import com.homebanking.domain.entity.Transfer;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface TransferRepository {

    /**
     * Guarda una transferencia nueva o actualiza una existente.
     * Garantiza consistencia ACID.
     */
    Transfer save(Transfer transfer);

    /**
     * Busca transferencia por ID.
     */
    Optional<Transfer> findById(UUID id);

    /**
     * Busca transferencia por idempotency key.
     * CRÍTICO para idempotencia: evita duplicados.
     */
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    /**
     * Obtiene todas las transferencias de una cuenta origen.
     * Útil para historial.
     */
    List<Transfer> findByOriginAccountId(UUID accountId);

    /**
     * Obtiene transferencias en estado PENDING.
     * Usado por servicio de procesamiento asincrónico.
     */
    List<Transfer> findPendingTransfers();

    /**
     * Obtiene transferencias fallidas y reintentables.
     * Usado por servicio de reintentos.
     */
    List<Transfer> findRetryableTransfers();
}



