package com.homebanking.application.usecase.transfer;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;

public interface RetryFailedTransferUseCase {
    TransferOutputResponse retryFailedTransfer(Long transferId);
}
