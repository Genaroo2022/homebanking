package com.homebanking.application.usecase.transfer;

import com.homebanking.application.dto.transfer.request.CreateTransferInputRequest;
import com.homebanking.application.dto.transfer.response.TransferOutputResponse;

// Use case es puro (sin conocer HTTP)
public interface CreateTransferUseCase {
    TransferOutputResponse createTransfer(CreateTransferInputRequest request);
}
