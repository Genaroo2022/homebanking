package com.homebanking.port.in.transfer;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;

import java.util.UUID;

public interface ProcessTransferInputPort {
    TransferOutputResponse processTransfer(UUID transferId);
}
