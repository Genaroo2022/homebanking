package com.homebanking.port.in.transfer;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;

import java.util.UUID;

public interface GetTransferInputPort {
    TransferOutputResponse getTransfer(UUID transferId, String requesterEmail);
}


