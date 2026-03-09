package com.homebanking.adapter.out.persistence.payment;

import com.homebanking.domain.enums.BillPaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bill_payments", indexes = {
        @Index(name = "idx_bill_payment_idempotency", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_bill_payment_account", columnList = "account_id"),
        @Index(name = "idx_bill_payment_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BillPaymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "biller_code", nullable = false, length = 40)
    private String billerCode;

    @Column(name = "reference", nullable = false, length = 80)
    private String reference;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BillPaymentStatus status;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public static BillPaymentJpaEntity of(
            UUID id,
            UUID accountId,
            String billerCode,
            String reference,
            BigDecimal amount,
            String idempotencyKey,
            BillPaymentStatus status,
            String failureReason,
            LocalDateTime createdAt,
            LocalDateTime processedAt,
            Long version) {
        return new BillPaymentJpaEntity(
                id,
                accountId,
                billerCode,
                reference,
                amount,
                idempotencyKey,
                status,
                failureReason,
                createdAt,
                processedAt,
                version
        );
    }
}

