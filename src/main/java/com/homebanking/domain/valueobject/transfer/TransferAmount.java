package com.homebanking.domain.valueobject.transfer;

import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.util.DomainErrorMessages;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: TransferAmount
 */
public final class TransferAmount {

    private static final BigDecimal MIN = new BigDecimal("0.01");
    private static final BigDecimal MAX = new BigDecimal("1000000.00");

    private final BigDecimal value;

    private TransferAmount(BigDecimal value) {
        this.value = value;
    }

    public static TransferAmount of(BigDecimal value) {
        if (value == null ||
                value.compareTo(MIN) < 0 ||
                value.compareTo(MAX) > 0) {
            throw new InvalidTransferDataException(
                    DomainErrorMessages.TRANSFER_AMOUNT_OUT_OF_RANGE
            );
        }
        return new TransferAmount(value);
    }

    public BigDecimal value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransferAmount that = (TransferAmount) o;
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}


