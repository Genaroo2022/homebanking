package com.homebanking.adapter.out.persistence.transfer;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity JPA: TransferJpaEntity

 * Mapeo O/R para persistencia.
 * Nota importante: Separado de la entidad de dominio Transfer.
 * La JPA entity es técnica, la entidad de dominio es lógica.
 */
@Entity
@Table(name = "transfers", indexes = {
        @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_origin_account", columnList = "origin_account_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class TransferJpaEntity {

    @Setter(AccessLevel.PACKAGE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 36)
    private String idempotencyKey;

    @Column(name = "origin_account_id", nullable = false)
    private Long originAccountId;

    @Column(name = "target_cbu", nullable = false, length = 22)
    private String targetCbu;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private com.homebanking.domain.enums.TransferStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "executed_at")
    LocalDateTime executedAt;

    @Column(name = "failed_at")
    LocalDateTime failedAt;

    @Column(name = "failure_reason", length = 500)
    String failureReason;

    @Column(name = "retry_count", nullable = false)
    Integer retryCount;

    @Column(name = "last_retry_at")
    LocalDateTime lastRetryAt;

    @Column(name = "version", nullable = false)
    @Version
    private Long version;

    public TransferJpaEntity(String idempotencyKey, Long originAccountId, String targetCbu,
                             BigDecimal amount, String description,
                             com.homebanking.domain.enums.TransferStatus status, LocalDateTime createdAt) {
        this.idempotencyKey = idempotencyKey;
        this.originAccountId = originAccountId;
        this.targetCbu = targetCbu;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.retryCount = 0;
        this.version = 0L;
    }
}