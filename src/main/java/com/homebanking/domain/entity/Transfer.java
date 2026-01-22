package com.homebanking.domain.entity;

import com.homebanking.domain.enums.TransferStatus;
import com.homebanking.domain.exception.InvalidTransferDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Entity: Transfer

 * Invariantes protegidas:
 * - Idempotency key único e inmutable (previene duplicados)
 * - Status solo puede transicionar en dirección válida
 * - Montos siempre positivos
 * - CBU válido de destino
 * - Nunca transferir a la misma cuenta

 * Responsabilidades de dominio:
 * - Validar datos de transferencia
 * - Controlar transiciones de estado
 * - Marcar timestamps de ejecución

 * No conoce:
 * - Cómo se persiste
 * - Cómo se notifica
 * - Cómo se auditoria
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transfer {

    private static final String CBU_REGEX = "^\\d+$";
    public static final int CBU_LENGTH = 22;

    // --- IDENTIDAD ---
    private Long id;
    private String idempotencyKey; // UUID para idempotencia

    // --- DATOS TRANSACCIONALES ---
    private Long originAccountId;
    private String targetCbu;
    private BigDecimal amount;
    private String description;

    // --- ESTADO Y AUDITORÍA ---
    private TransferStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;
    private LocalDateTime failedAt;
    private String failureReason;

    // --- CONTROL DE INTENTOS ---
    private Integer retryCount;
    private LocalDateTime lastRetryAt;

    /**
     * Constructor: Crear nueva transferencia (sin ID).
     * Genera automáticamente idempotency key.
     */
    public Transfer(Long originAccountId, String targetCbu,
                    BigDecimal amount, String description) {
        validateTransferData(originAccountId, targetCbu, amount, description);

        this.originAccountId = originAccountId;
        this.targetCbu = targetCbu;
        this.amount = amount;
        this.description = description;
        this.idempotencyKey = UUID.randomUUID().toString();
        this.status = TransferStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.retryCount = 0;
    }

    /**
     * Factory Method: Crear nueva transferencia con idempotency key provista por el cliente.
     */
    public static Transfer createWithIdempotencyKey(
            Long originAccountId, String targetCbu, BigDecimal amount,
            String description, String idempotencyKey) {

        validateTransferData(originAccountId, targetCbu, amount, description);
        validateIdempotencyKey(idempotencyKey);

        Transfer transfer = new Transfer();
        transfer.originAccountId = originAccountId;
        transfer.targetCbu = targetCbu;
        transfer.amount = amount;
        transfer.description = description;
        transfer.idempotencyKey = idempotencyKey;
        transfer.status = TransferStatus.PENDING;
        transfer.createdAt = LocalDateTime.now();
        transfer.retryCount = 0;
        return transfer;
    }

    /**
     * Factory Method: Reconstitución desde persistencia.
     */
    public static Transfer withId(
            Long id, String idempotencyKey, Long originAccountId, String targetCbu,
            BigDecimal amount, String description, TransferStatus status,
            LocalDateTime createdAt, LocalDateTime executedAt, LocalDateTime failedAt,
            String failureReason, Integer retryCount, LocalDateTime lastRetryAt) {

        validateStructuralData(id, idempotencyKey, createdAt);
        validateTransferData(originAccountId, targetCbu, amount, description);

        return hydrate(id, idempotencyKey, originAccountId, targetCbu, amount,
                description, status, createdAt, executedAt, failedAt,
                failureReason, retryCount, lastRetryAt);
    }

    private static Transfer hydrate(
            Long id, String idempotencyKey, Long originAccountId, String targetCbu,
            BigDecimal amount, String description, TransferStatus status,
            LocalDateTime createdAt, LocalDateTime executedAt, LocalDateTime failedAt,
            String failureReason, Integer retryCount, LocalDateTime lastRetryAt) {

        Transfer transfer = new Transfer();
        transfer.id = id;
        transfer.idempotencyKey = idempotencyKey;
        transfer.originAccountId = originAccountId;
        transfer.targetCbu = targetCbu;
        transfer.amount = amount;
        transfer.description = description;
        transfer.status = status;
        transfer.createdAt = createdAt;
        transfer.executedAt = executedAt;
        transfer.failedAt = failedAt;
        transfer.failureReason = failureReason;
        transfer.retryCount = retryCount;
        transfer.lastRetryAt = lastRetryAt;
        return transfer;
    }

    // ============================================
    // BUSINESS METHODS (Transiciones de Estado)
    // ============================================

    /**
     * Marca la transferencia como completada.
     * Transición: PENDING -> COMPLETED
     */
    public void markAsCompleted() {
        validateCanTransition();
        this.status = TransferStatus.COMPLETED;
        this.executedAt = LocalDateTime.now();
    }

    /**
     * Marca la transferencia como fallida.
     * Transición: PENDING -> FAILED
     * Registra razón del fallo y timestamp.
     */
    public void markAsFailed(String reason) {
        validateCanTransition();
        validateFailureReason(reason);
        this.status = TransferStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
    }

    /**
     * Marca la transferencia como rechazada.
     * Transición: PENDING -> REJECTED
     */
    public void markAsRejected(String reason) {
        validateCanTransition();
        validateFailureReason(reason);
        this.status = TransferStatus.REJECTED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
    }

    /**
     * Incrementa contador de reintentos.
     * Usado para rastrear intentos de procesar la transferencia.
     */
    public void incrementRetryCount() {
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
        this.lastRetryAt = LocalDateTime.now();
    }

    /**
     * Verifica si la transferencia es exitosa.
     */
    public boolean isSuccessful() {
        return this.status == TransferStatus.COMPLETED;
    }

    /**
     * Verifica si la transferencia puede ser reintentada.
     */
    public boolean canBeRetried() {
        return this.status == TransferStatus.FAILED &&
                (this.retryCount == null || this.retryCount < 3);
    }

    /**
     * Verifica si la transferencia está en estado final.
     */
    public boolean isFinalized() {
        return this.status == TransferStatus.COMPLETED ||
                this.status == TransferStatus.REJECTED;
    }

    // ============================================
    // VALIDACIONES (Private Static)
    // ============================================

    private static void validateStructuralData(Long id, String idempotencyKey, LocalDateTime createdAt) {
        if (id == null) {
            throw new InvalidTransferDataException(DomainErrorMessages.ID_REQUIRED);
        }
        validateIdempotencyKey(idempotencyKey);
        if (createdAt == null) {
            throw new InvalidTransferDataException(DomainErrorMessages.CREATED_AT_REQUIRED);
        }
    }

    private static void validateTransferData(Long originAccountId, String targetCbu,
                                             BigDecimal amount, String description) {
        validateOriginAccount(originAccountId);
        validateAmount(amount);
        validateTarget(targetCbu);
        validateDescription(description);
    }

    private void validateCanTransition() {
        if (this.status != TransferStatus.PENDING) {
            throw new InvalidTransferDataException(
                    DomainErrorMessages.TRANSFER_ALREADY_FINALIZED
            );
        }
    }

    private static void validateFailureReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidTransferDataException(
                    DomainErrorMessages.TRANSFER_FAILURE_REASON_REQUIRED
            );
        }
    }

    private static void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidTransferDataException(DomainErrorMessages.IDEMPOTENCY_KEY_REQUIRED);
        }
    }

    private static void validateOriginAccount(Long originAccountId) {
        if (originAccountId == null || originAccountId <= 0) {
            throw new InvalidTransferDataException(
                    DomainErrorMessages.ORIGIN_ACCOUNT_ID_INVALID
            );
        }
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferDataException(
                    DomainErrorMessages.TRANSFER_AMOUNT_INVALID
            );
        }
    }

    private static void validateTarget(String targetCbu) {
        if (targetCbu == null || targetCbu.isBlank()) {
            throw new InvalidTransferDataException(DomainErrorMessages.CBU_REQUIRED);
        }
        if (!Pattern.matches(CBU_REGEX, targetCbu)) {
            throw new InvalidTransferDataException(DomainErrorMessages.CBU_ONLY_NUMBERS);
        }
        if (targetCbu.length() != CBU_LENGTH) {
            throw new InvalidTransferDataException(DomainErrorMessages.CBU_INVALID_LENGTH);
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new InvalidTransferDataException(
                    DomainErrorMessages.TRANSFER_DESC_REQUIRED
            );
        }
    }
}
