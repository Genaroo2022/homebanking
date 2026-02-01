package com.homebanking.domain.exception.transfer;

import com.homebanking.domain.exception.common.DomainException;

import java.util.UUID;

public class TransferNotFoundException extends DomainException {

    private final UUID transferId;

    public TransferNotFoundException(String message, UUID transferId) {
        super(message);
        this.transferId = transferId;
    }

    public UUID getTransferId() {
        return transferId;
    }
}


