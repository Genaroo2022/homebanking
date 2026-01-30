package com.homebanking.domain.entity;

import com.homebanking.domain.enums.TransferStatus;
import com.homebanking.domain.event.TransferCompletedEvent;
import com.homebanking.domain.event.TransferFailedEvent;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.domain.valueobject.common.Cbu;
import com.homebanking.domain.valueobject.transfer.IdempotencyKey;
import com.homebanking.domain.valueobject.transfer.RetryPolicy;
import com.homebanking.domain.valueobject.transfer.TransferAmount;
import com.homebanking.domain.valueobject.transfer.TransferDescription;
import com.homebanking.domain.valueobject.transfer.TransferFailure;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AGGREGATE ROOT: Transfer
 *
 * Responsabilidades:
 * ✓ Guardar estado de la transferencia
 * ✓ Garantizar invariantes del dominio
 * ✓ Transiciones de estado controladas
 * ✓ Publicar eventos de dominio
 *
 * No responsable de:
 * ✗ Validar datos (→ TransferValidator)
 * ✗ Orquestar transiciones (→ TransferStateService)
 * ✗ Persistencia (→ Repository)
 * ✗ Persistir eventos (→ Event publisher)
 *
 * Invariantes:
 * • Si status == COMPLETED, executedAt != null
 * • Si status == FAILED, failure != null
 * • Si status == REJECTED, failure != null
 * • No hay forma de violar estos invariantes sin pasar por métodos controlados
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transfer {

    // ==================== IDENTIDAD ====================
    private Long id;
    private IdempotencyKey idempotencyKey;

    // ==================== DATOS TRANSACCIONALES ====================
    private Long originAccountId;
    private Cbu targetCbu;
    private TransferAmount amount;
    private TransferDescription description;

    // ==================== ESTADO ====================
    private TransferStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;

    // ==================== REINTENTOS Y FALLOS ====================
    private RetryPolicy retryPolicy;
    private TransferFailure failure;

    // ==================== EVENTOS DE DOMINIO ====================
    private List<Object> domainEvents = new ArrayList<>();

    // ==================== FACTORY METHODS ====================

    /**
     * Crea una nueva transferencia (CREACIÓN).
     *
     * Factory para crear transferencias nuevas desde cero.
     * Valida datos de entrada y garantiza estado inicial consistente.
     *
     * @param originAccountId ID de la cuenta origen
     * @param targetCbu CBU de la cuenta destino
     * @param amount Monto a transferir
     * @param description Descripción de la transferencia
     * @param idempotencyKey Clave de idempotencia
     * @return Transfer en estado PENDING con retryPolicy inicial
     * @throws InvalidTransferDataException si datos son inválidos
     */
    public static Transfer create(
            Long originAccountId,
            Cbu targetCbu,
            TransferAmount amount,
            TransferDescription description,
            IdempotencyKey idempotencyKey) {

        Transfer transfer = new Transfer();
        transfer.id = null;                                    // ← Sin ID aún (lo asigna BD)
        transfer.originAccountId = originAccountId;
        transfer.targetCbu = targetCbu;
        transfer.amount = amount;
        transfer.description = description;
        transfer.idempotencyKey = idempotencyKey;
        transfer.status = TransferStatus.PENDING;             // ← Estado inicial
        transfer.createdAt = LocalDateTime.now();
        transfer.retryPolicy = RetryPolicy.initial();         // ← 0 reintentos
        transfer.domainEvents = new ArrayList<>();

        return transfer;
    }

    /**
     * Reconstituyir una transferencia desde persistencia (RECONSTITUCIÓN).
     *
     * Factory para cargar transferencias desde la base de datos.
     * Reconstituyir estado completo incluyendo reintentos y fallos.
     *
     * @param id ID de la transferencia (desde BD)
     * @param idempotencyKey Clave de idempotencia
     * @param originAccountId ID de la cuenta origen
     * @param targetCbu CBU de la cuenta destino
     * @param amount Monto de la transferencia
     * @param description Descripción
     * @param status Estado actual
     * @param createdAt Fecha de creación
     * @param executedAt Fecha de ejecución (si completada)
     * @param failureReason Razón del fallo (si falló)
     * @param failedAt Fecha del fallo (si falló)
     * @param retryCount Cantidad de reintentos
     * @param lastRetryAt Fecha del último reintento
     * @return Transfer con estado reconstituyido
     */
    public static Transfer reconstruct(
            Long id,
            IdempotencyKey idempotencyKey,
            Long originAccountId,
            Cbu targetCbu,
            TransferAmount amount,
            TransferDescription description,
            TransferStatus status,
            LocalDateTime createdAt,
            LocalDateTime executedAt,
            String failureReason,
            LocalDateTime failedAt,
            Integer retryCount,
            LocalDateTime lastRetryAt) {

        Transfer transfer = new Transfer();
        transfer.id = id;                                           // ← Con ID desde BD
        transfer.idempotencyKey = idempotencyKey;
        transfer.originAccountId = originAccountId;
        transfer.targetCbu = targetCbu;
        transfer.amount = amount;
        transfer.description = description;
        transfer.status = status;                                   // ← Puede ser cualquiera
        transfer.createdAt = createdAt;
        transfer.executedAt = executedAt;
        transfer.retryPolicy = RetryPolicy.of(retryCount, lastRetryAt);

        // Reconstituyir failure si existe
        if (failedAt != null || (failureReason != null && !failureReason.isBlank())) {
            transfer.failure = TransferFailure.of(failureReason, failedAt);
        }

        transfer.domainEvents = new ArrayList<>();  // ← Los eventos se publican al persistir

        return transfer;
    }

    // ==================== BUSINESS METHODS: STATE TRANSITIONS ====================

    /**
     * Transición: PENDING → PROCESSING | FAILED → PROCESSING (reintento)
     *
     * Marca la transferencia como siendo procesada.
     * Puede ocurrir desde PENDING (primera vez) o FAILED (reintento).
     *
     * @throws InvalidTransferDataException si la transición no es válida
     */
    public void markAsProcessing() {
        if (status == TransferStatus.PENDING) {
            this.status = TransferStatus.PROCESSING;
            return;
        }

        if (status == TransferStatus.FAILED && isRetryable()) {
            this.status = TransferStatus.PROCESSING;
            this.retryPolicy = retryPolicy.withRetryIncremented();
            return;
        }

        throw new InvalidTransferDataException(
                String.format(DomainErrorMessages.INVALID_PROCESSING_TRANSITION, status)
        );
    }

    /**
     * Transición: PROCESSING → COMPLETED
     *
     * Marca la transferencia como completada exitosamente.
     * Solo es válido desde estado PROCESSING.

     * INVARIANTE: Si status == COMPLETED, executedAt != null
     *
     * @throws InvalidTransferDataException si no está en PROCESSING
     */
    public void markAsCompleted() {
        if (status != TransferStatus.PROCESSING) {
            throw new InvalidTransferDataException(
                    String.format(DomainErrorMessages.ONLY_PROCESSING_CAN_COMPLETE, status)
            );
        }

        this.status = TransferStatus.COMPLETED;
        this.executedAt = LocalDateTime.now();

        // Publicar evento de dominio
        this.domainEvents.add(new TransferCompletedEvent(
                this.id,
                this.originAccountId,
                this.targetCbu.value(),
                this.amount.value(),
                LocalDateTime.now()
        ));
    }

    /**
     * Transición: PROCESSING → FAILED

     * Marca la transferencia como fallida (error temporal, recuperable).
     * Solo es válido desde estado PROCESSING.
     * El fallo se puede reintentar más tarde.

     * INVARIANTE: Si status == FAILED, failure != null
     *
     * @param reason Razón del fallo
     * @throws InvalidTransferDataException si no está en PROCESSING
     */
    public void markAsFailed(String reason) {
        if (status != TransferStatus.PROCESSING) {
            throw new InvalidTransferDataException(
                    String.format(DomainErrorMessages.ONLY_PROCESSING_CAN_FAIL, status)
            );
        }

        this.status = TransferStatus.FAILED;
        this.failure = TransferFailure.of(reason);

        // Publicar evento de dominio
        this.domainEvents.add(new TransferFailedEvent(
                this.id,
                this.originAccountId,
                this.targetCbu.value(),
                this.amount.value(),
                reason,
                LocalDateTime.now()
        ));
    }

    /**
     * Transición: PROCESSING → REJECTED | FAILED (no retryable) → REJECTED

     * Marca la transferencia como rechazada (error permanente, no recuperable).
     * Es terminal: no se puede reintentar.

     * INVARIANTE: Si status == REJECTED, failure != null
     *
     * @param reason Razón del rechazo
     * @throws InvalidTransferDataException si no puede rechazarse desde este estado
     */
    public void markAsRejected(String reason) {
        // Desde PROCESSING (rechazo inmediato)
        if (status == TransferStatus.PROCESSING) {
            this.status = TransferStatus.REJECTED;
            this.failure = TransferFailure.of(reason);
            return;
        }

        // Desde FAILED sin reintentos disponibles (agotamiento de reintentos)
        if (status == TransferStatus.FAILED && !isRetryable()) {
            this.status = TransferStatus.REJECTED;
            this.failure = TransferFailure.of(reason);
            return;
        }

        throw new InvalidTransferDataException(
                String.format(DomainErrorMessages.CANNOT_REJECT_TRANSFER, status)
        );
    }

    // ==================== QUERY METHODS: STATE CHECKS ====================

    /**
     * ¿Está la transferencia en un estado activo (siendo procesada)?
     */
    public boolean isActive() {
        return status == TransferStatus.PENDING || status == TransferStatus.PROCESSING;
    }

    /**
     * ¿Está la transferencia siendo procesada actualmente?
     */
    public boolean isProcessing() {
        return status == TransferStatus.PROCESSING;
    }

    /**
     * ¿Está la transferencia en un estado terminal (completada o rechazada)?
     */
    public boolean isTerminal() {
        return status == TransferStatus.COMPLETED || status == TransferStatus.REJECTED;
    }

    /**
     * ¿Se completó la transferencia exitosamente?
     */
    public boolean isSuccessful() {
        return status == TransferStatus.COMPLETED;
    }

    /**
     * ¿Falló la transferencia (temporalmente)?
     */
    public boolean isFailed() {
        return status == TransferStatus.FAILED;
    }

    /**
     * ¿Fue rechazada la transferencia (permanentemente)?
     */
    public boolean isRejected() {
        return status == TransferStatus.REJECTED;
    }

    /**
     * ¿Puede reintentarse la transferencia?

     * Verdadero si está en FAILED y tiene reintentos disponibles.
     */
    public boolean isRetryable() {
        return status == TransferStatus.FAILED && retryPolicy.isRetryable();
    }

    /**
     * ¿Puede procesarse esta transferencia ahora?

     * Verdadero si está en PENDING o en FAILED con reintentos disponibles.
     */
    public boolean isEligibleForProcessing() {
        return status == TransferStatus.PENDING ||
                (status == TransferStatus.FAILED && isRetryable());
    }

    // ==================== GETTERS PARA INFORMACIÓN ====================

    /**
     * Obtiene la cantidad de reintentos realizados.
     */
    public Integer getRetryCount() {
        return retryPolicy.getRetryCount();
    }

    /**
     * Obtiene la cantidad de reintentos restantes.
     */
    public int getRetriesRemaining() {
        return retryPolicy.retriesRemaining();
    }

    /**
     * Obtiene la fecha del último reintento.
     */
    public LocalDateTime getLastRetryAt() {
        return retryPolicy.getLastRetryAt();
    }

    /**
     * Obtiene la razón del fallo (si la transferencia falló).
     */
    public String getFailureReason() {
        return failure != null ? failure.getReason() : null;
    }

    /**
     * Obtiene la fecha del fallo (si la transferencia falló).
     */
    public LocalDateTime getFailedAt() {
        return failure != null ? failure.getFailedAt() : null;
    }

    // ==================== EVENT SOURCING ====================

    /**
     * Obtiene los eventos de dominio publicados.

     * Los eventos se publican cuando ocurren cambios de estado importantes.
     * El publisher es responsable de:
     * - Persistir estos eventos
     * - Publicarlos a otros contextos acotados
     * - Limpiar la lista después de publicar
     */
    public List<Object> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    /**
     * Limpia los eventos después de ser publicados.

     * Debe llamarse después de que el publisher haya procesado los eventos.
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}