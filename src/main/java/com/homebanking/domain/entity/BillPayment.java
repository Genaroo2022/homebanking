package com.homebanking.domain.entity;

import com.homebanking.domain.enums.BillPaymentStatus;
import com.homebanking.domain.exception.payment.InvalidBillPaymentDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BillPayment {

    private UUID id;
    private UUID accountId;
    private String billerCode;
    private String reference;
    private BigDecimal amount;
    private String idempotencyKey;
    private BillPaymentStatus status;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public static BillPayment create(
            UUID accountId,
            String billerCode,
            String reference,
            BigDecimal amount,
            String idempotencyKey) {
        validate(accountId, billerCode, reference, amount, idempotencyKey);

        BillPayment payment = new BillPayment();
        payment.accountId = accountId;
        payment.billerCode = billerCode.trim().toUpperCase();
        payment.reference = reference.trim();
        payment.amount = amount;
        payment.idempotencyKey = idempotencyKey.trim();
        payment.status = BillPaymentStatus.PENDING;
        payment.createdAt = LocalDateTime.now();
        return payment;
    }

    public static BillPayment reconstruct(
            UUID id,
            UUID accountId,
            String billerCode,
            String reference,
            BigDecimal amount,
            String idempotencyKey,
            BillPaymentStatus status,
            String failureReason,
            LocalDateTime createdAt,
            LocalDateTime processedAt) {
        if (id == null) {
            throw new InvalidBillPaymentDataException(DomainErrorMessages.ID_REQUIRED);
        }
        validate(accountId, billerCode, reference, amount, idempotencyKey);
        if (status == null) {
            throw new InvalidBillPaymentDataException(DomainErrorMessages.BILL_PAYMENT_STATUS_REQUIRED);
        }
        BillPayment payment = new BillPayment();
        payment.id = id;
        payment.accountId = accountId;
        payment.billerCode = billerCode.trim().toUpperCase();
        payment.reference = reference.trim();
        payment.amount = amount;
        payment.idempotencyKey = idempotencyKey.trim();
        payment.status = status;
        payment.failureReason = failureReason;
        payment.createdAt = createdAt;
        payment.processedAt = processedAt;
        return payment;
    }

    public void markAsPaid() {
        if (status != BillPaymentStatus.PENDING) {
            throw new InvalidBillPaymentDataException(
                    String.format(DomainErrorMessages.BILL_PAYMENT_INVALID_STATUS_TRANSITION, status));
        }
        this.status = BillPaymentStatus.PAID;
        this.processedAt = LocalDateTime.now();
        this.failureReason = null;
    }

    public void markAsFailed(String reason) {
        if (status != BillPaymentStatus.PENDING) {
            throw new InvalidBillPaymentDataException(
                    String.format(DomainErrorMessages.BILL_PAYMENT_INVALID_STATUS_TRANSITION, status));
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidBillPaymentDataException(DomainErrorMessages.BILL_PAYMENT_FAILURE_REASON_REQUIRED);
        }
        this.status = BillPaymentStatus.FAILED;
        this.processedAt = LocalDateTime.now();
        this.failureReason = reason.trim();
    }

    private static void validate(
            UUID accountId,
            String billerCode,
            String reference,
            BigDecimal amount,
            String idempotencyKey) {
        if (accountId == null) {
            throw new InvalidBillPaymentDataException(DomainErrorMessages.ORIGIN_ACCOUNT_ID_INVALID);
        }
        if (billerCode == null || billerCode.isBlank()) {
            throw new InvalidBillPaymentDataException(DomainErrorMessages.BILLER_CODE_REQUIRED);
        }
        if (reference == null || reference.isBlank()) {
            throw new InvalidBillPaymentDataException(DomainErrorMessages.BILL_REFERENCE_REQUIRED);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBillPaymentDataException(DomainErrorMessages.TRANSFER_AMOUNT_INVALID);
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidBillPaymentDataException(DomainErrorMessages.IDEMPOTENCY_KEY_REQUIRED);
        }
    }
}

