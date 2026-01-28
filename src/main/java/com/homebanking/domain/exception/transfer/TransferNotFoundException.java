package com.homebanking.domain.exception.transfer;

import com.homebanking.domain.exception.common.DomainException;

public class TransferNotFoundException extends DomainException {

    private final Long transferId;

    public TransferNotFoundException(String message, Long transferId) {
        super(message);
        this.transferId = transferId;
    }

    public Long getTransferId() {
        return transferId;
    }
}
