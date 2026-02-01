package com.homebanking.application.service.transfer.action;

import com.homebanking.application.dto.transfer.response.TransferProcessingResult;
import com.homebanking.domain.entity.Transfer;

public interface TransferProcessingAction {
    TransferProcessingResult.Outcome outcome();

    void apply(Transfer transfer, TransferProcessingResult result);
}


