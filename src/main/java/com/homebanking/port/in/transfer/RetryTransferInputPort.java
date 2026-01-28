package com.homebanking.port.in.transfer;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;

public interface RetryTransferInputPort {
    TransferOutputResponse retryFailedTransfer(Long transferId);
}
