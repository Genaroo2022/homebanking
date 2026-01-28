package com.homebanking.application.usecase.transfer;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;

public interface GetTransferUseCase {
    TransferOutputResponse getTransfer(Long transferId);
}
