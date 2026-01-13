package com.homebanking.domain.entity;

import com.homebanking.domain.enums.TransferStatus;
import com.homebanking.domain.exception.InvalidTransferDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transfer {
    private static final String CBU_REGEX = "^\\d+$";

    private Long id;
    private Long originAccountId;
    private String targetCbu;
    private BigDecimal amount;
    private String description;
    private TransferStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;

    // To create a new card (without ID)
    public Transfer(Long originAccountId, String targetCbu, BigDecimal amount, String description) {
        validateTransferData(originAccountId, targetCbu, amount, description);

        this.originAccountId = originAccountId;
        this.targetCbu = targetCbu;
        this.amount = amount;
        this.description = description;
        this.status = TransferStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Factory Method: Reconstitution from Persistence
    public static Transfer withId(Long id, Long originAccountId, String targetCbu, BigDecimal amount,
                                  String description, TransferStatus status, LocalDateTime createdAt, LocalDateTime executedAt) {
        validateStructuralData(id, createdAt);
        validateTransferData(originAccountId, targetCbu, amount, description);
        return hydrate(id, originAccountId, targetCbu, amount, description, status, createdAt, executedAt);
    }
    private static Transfer hydrate(Long id, Long originAccountId, String targetCbu, BigDecimal amount,
                                    String description, TransferStatus status, LocalDateTime createdAt, LocalDateTime executedAt) {
        Transfer transfer = new Transfer();
        transfer.id = id;
        transfer.originAccountId = originAccountId;
        transfer.targetCbu = targetCbu;
        transfer.amount = amount;
        transfer.description = description;
        transfer.status = status;
        transfer.createdAt = createdAt;
        transfer.executedAt = executedAt;
        return transfer;
    }

    // --- BUSINESS METHODS ---

    public void markAsCompleted() {
        validateStateTransition();
        this.status = TransferStatus.COMPLETED;
        this.executedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        validateStateTransition();
        this.status = TransferStatus.FAILED;
        this.executedAt = LocalDateTime.now();
    }

    public boolean isSuccessful() {
        return this.status == TransferStatus.COMPLETED;
    }

    // --- VALIDATIONS (Private Static) ---

    private static void validateStructuralData(Long id, LocalDateTime createdAt) {
        if (id == null) {
            throw new InvalidTransferDataException(DomainErrorMessages.ID_REQUIRED);
        }
        if (createdAt == null) {
            throw new InvalidTransferDataException(DomainErrorMessages.CREATED_AT_REQUIRED);
        }
    }

    private static void validateTransferData(Long originAccountId, String targetCbu, BigDecimal amount, String description) {
        validateOriginAccount(originAccountId);
        validateAmount(amount);
        validateTarget(targetCbu);
        validateDescription(description);
    }

    private static void validateStateTransition(Transfer transfer) {
        if (transfer.status != TransferStatus.PENDING) {
            throw new InvalidTransferDataException(DomainErrorMessages.TRANSFER_ALREADY_FINALIZED);
        }
    }

    private void validateStateTransition() {
        if (this.status != TransferStatus.PENDING) {
            throw new InvalidTransferDataException(DomainErrorMessages.TRANSFER_ALREADY_FINALIZED);
        }
    }

    private static void validateOriginAccount(Long originAccountId) {
        if (originAccountId == null || originAccountId <= 0) {
            throw new InvalidTransferDataException(DomainErrorMessages.ORIGIN_ACCOUNT_ID_INVALID);
        }
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferDataException(DomainErrorMessages.TRANSFER_AMOUNT_INVALID);
        }
    }

    private static void validateTarget(String targetCbu) {
        if (targetCbu == null || targetCbu.isBlank()) {
            throw new InvalidTransferDataException(DomainErrorMessages.CBU_REQUIRED);
        }
        if (!Pattern.matches(CBU_REGEX, targetCbu)) {
            throw new InvalidTransferDataException(DomainErrorMessages.CBU_ONLY_NUMBERS);
        }
        if (targetCbu.length() != Account.CBU_LENGTH) {
            throw new InvalidTransferDataException(DomainErrorMessages.CBU_INVALID_LENGTH);
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new InvalidTransferDataException(DomainErrorMessages.TRANSFER_DESC_REQUIRED);
        }
    }
}