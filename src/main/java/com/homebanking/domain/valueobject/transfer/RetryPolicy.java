package com.homebanking.domain.valueobject.transfer;

import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.time.LocalDateTime;

public final class RetryPolicy {

    private static final int MAX_RETRIES = 3;

    private final int retryCount;
    private final LocalDateTime lastRetryAt;

    private RetryPolicy(int retryCount, LocalDateTime lastRetryAt) {
        this.retryCount = retryCount;
        this.lastRetryAt = lastRetryAt;
    }

    public static RetryPolicy initial() {
        return new RetryPolicy(0, null);
    }

    public static RetryPolicy of(Integer retryCount, LocalDateTime lastRetryAt) {
        int count = retryCount == null ? 0 : retryCount;
        if (count < 0) {
            throw new InvalidTransferDataException(DomainErrorMessages.TRANSFER_RETRYCOUNT_NEGATIVE);
        }
        if (count > 0 && lastRetryAt == null) {
            throw new InvalidTransferDataException(DomainErrorMessages.TRANSFER_LAST_RETRY_REQUIRED);
        }
        if (count > MAX_RETRIES) {
            throw new InvalidTransferDataException(
                    String.format(DomainErrorMessages.MAX_RETRIES_EXCEEDED, MAX_RETRIES, count)
            );
        }
        return new RetryPolicy(count, lastRetryAt);
    }

    public RetryPolicy withRetryIncremented() {
        if (retryCount >= MAX_RETRIES) {
            throw new InvalidTransferDataException(
                    String.format(DomainErrorMessages.MAX_RETRIES_EXCEEDED, MAX_RETRIES, retryCount)
            );
        }
        return new RetryPolicy(retryCount + 1, LocalDateTime.now());
    }

    public boolean isRetryable() {
        return retryCount < MAX_RETRIES;
    }

    public int retriesRemaining() {
        return Math.max(0, MAX_RETRIES - retryCount);
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public LocalDateTime getLastRetryAt() {
        return lastRetryAt;
    }
}
