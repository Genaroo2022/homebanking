package com.homebanking.domain.policy.transition;

import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.policy.transfer.TransferStateTransition;
import com.homebanking.domain.util.DomainErrorMessages;

public class TakeForProcessingTransition implements TransferStateTransition {
    @Override
    public void execute(Transfer transfer) {
        if (!isApplicable(transfer)) {
            throw new InvalidTransferDataException(
                    String.format(DomainErrorMessages.INVALID_PROCESSING_TRANSITION, transfer.getStatus())
            );
        }
        transfer.markAsProcessing();
    }

    @Override
    public boolean isApplicable(Transfer transfer) {
        return transfer.isEligibleForProcessing();
    }
}


