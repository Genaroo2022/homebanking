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

        public Transfer(Long id, Long originAccountId, String targetCbu, BigDecimal amount, String description) {
            validateOriginAccount(originAccountId);
            validateAmount(amount);
            validateTarget(targetCbu);
            validateDescription(description);

            this.id = id;
            this.originAccountId = originAccountId;
            this.targetCbu = targetCbu;
            this.amount = amount;
            this.description = description;
            this.status = TransferStatus.PENDING;
            this.createdAt = LocalDateTime.now();
        }

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

        // --- PRIVATE VALIDATIONS ---

        private void validateStateTransition() {
            if (this.status != TransferStatus.PENDING) {
                throw new InvalidTransferDataException(DomainErrorMessages.TRANSFER_ALREADY_FINALIZED);
            }
        }

        private void validateOriginAccount(Long originAccountId) {
            if (originAccountId == null || originAccountId <= 0) {
                throw new InvalidTransferDataException(DomainErrorMessages.ORIGIN_ACCOUNT_ID_INVALID);
            }
        }

        private void validateAmount(BigDecimal amount) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidTransferDataException(DomainErrorMessages.TRANSFER_AMOUNT_INVALID);
            }
        }

        private void validateTarget(String targetCbu) {
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

        private void validateDescription(String description) {
            if (description == null || description.isBlank()) {
                throw new InvalidTransferDataException(DomainErrorMessages.TRANSFER_DESC_REQUIRED);
            }
        }

        public boolean isSuccessful() {
            return this.status == TransferStatus.COMPLETED;
        }
    }