package com.homebanking.domain.valueobject.transfer;

import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.time.LocalDateTime;

public final class TransferFailure {

    private final String reason;
    private final LocalDateTime failedAt;

    private TransferFailure(String reason, LocalDateTime failedAt) {
        this.reason = reason;
        this.failedAt = failedAt;
    }

    public static TransferFailure of(String reason) {
        return of(reason, LocalDateTime.now());
    }

    public static TransferFailure of(String reason, LocalDateTime failedAt) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidTransferDataException(DomainErrorMessages.TRANSFER_FAILURE_REASON_REQUIRED);
        }
        LocalDateTime timestamp = failedAt != null ? failedAt : LocalDateTime.now();
        return new TransferFailure(reason, timestamp);
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }
}


