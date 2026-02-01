package com.homebanking.adapter.out.persistence.transfer;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


import com.homebanking.domain.enums.TransferStatus;
import com.homebanking.domain.entity.Transfer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * JPA Entity: TransferJpaEntity

 * Mapeo O/R para persistencia.

 * Características:
 * ✓ Constructor privado (solo factory method)
 * ✓ Sin setters públicos (solo Lombok @Getter)
 * ✓ Factory method: fromDomain()
 * ✓ Índices optimizados
 * ✓ Auditoría con @Version

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
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // ← Constructor privado con todos los parámetros
public class TransferJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 36)
    private String idempotencyKey;

    @Column(name = "origin_account_id", nullable = false)
    private UUID originAccountId;

    @Column(name = "target_cbu", nullable = false, length = 22)
    private String targetCbu;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;

    @Column(name = "version", nullable = false)
    @Version
    private Long version;

    // ==================== FACTORY METHOD ====================

    /**
     * Factory method: Crea TransferJpaEntity desde Transfer domain.

     * Este es el ÚNICO forma de crear una instancia válida.
     * Encapsula toda la lógica de mapeo.
     * Garantiza estado consistente.

     * @param domain Transfer entity del dominio
     * @return TransferJpaEntity totalmente inicializado
     */
    public static TransferJpaEntity fromDomain(Transfer domain) {
        return new TransferJpaEntity(
                domain.getId(),                           // id
                domain.getIdempotencyKey().value(),       // idempotencyKey
                domain.getOriginAccountId(),              // originAccountId
                domain.getTargetCbu().value(),            // targetCbu
                domain.getAmount().value(),               // amount
                domain.getDescription().value(),          // description
                domain.getStatus(),                       // status
                domain.getCreatedAt(),                    // createdAt
                domain.getExecutedAt(),                   // executedAt
                domain.getFailedAt(),                     // failedAt
                domain.getFailureReason(),                // failureReason
                domain.getRetryCount(),                   // retryCount
                domain.getLastRetryAt(),                  // lastRetryAt
                0L                                        // version (nueva entidad = 0)
        );
    }
}

