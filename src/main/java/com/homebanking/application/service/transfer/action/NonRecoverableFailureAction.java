package com.homebanking.application.service.transfer.action;

import com.homebanking.application.dto.transfer.response.TransferProcessingResult;
import com.homebanking.domain.entity.Transfer;
import org.springframework.stereotype.Component;

@Component
public class NonRecoverableFailureAction implements TransferProcessingAction {

    @Override
    public TransferProcessingResult.Outcome outcome() {
        return TransferProcessingResult.Outcome.NON_RECOVERABLE_FAILURE;
    }

    @Override
    public void apply(Transfer transfer, TransferProcessingResult result) {
        String reason = result.errorMessage()
                .orElse("Error no recuperable durante procesamiento.");
        transfer.markAsRejected(reason);
    }
}
