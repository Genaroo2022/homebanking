package com.homebanking.application.service.transfer.action;

import com.homebanking.application.dto.transfer.response.TransferProcessingResult;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.policy.transition.MarkAsFailedTransition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RetryableFailureAction implements TransferProcessingAction {

    @Override
    public TransferProcessingResult.Outcome outcome() {
        return TransferProcessingResult.Outcome.RECOVERABLE_FAILURE;
    }

    @Override
    public void apply(Transfer transfer, TransferProcessingResult result) {
        new MarkAsFailedTransition("Error temporal durante procesamiento. Se reintentara automaticamente.")
                .execute(transfer);
        log.warn("Transferencia marcada para reintento: id={}, attempt={}",
                transfer.getId(), transfer.getRetryCount());
    }
}


