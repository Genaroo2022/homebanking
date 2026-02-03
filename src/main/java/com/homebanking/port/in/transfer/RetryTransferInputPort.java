package com.homebanking.port.in.transfer;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;

import java.util.UUID;

public interface RetryTransferInputPort {
    TransferOutputResponse retryFailedTransfer(UUID transferId);
    TransferOutputResponse retryFailedTransfer(UUID transferId, String requesterEmail);
}


