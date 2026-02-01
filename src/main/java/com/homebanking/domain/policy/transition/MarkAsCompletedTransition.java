package com.homebanking.domain.policy.transition;

import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.enums.TransferStatus;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.policy.transfer.TransferStateTransition;
import com.homebanking.domain.util.DomainErrorMessages;

public class MarkAsCompletedTransition implements TransferStateTransition {
    @Override
    public void execute(Transfer transfer) {
        if (!isApplicable(transfer)) {
            throw new InvalidTransferDataException(
                    String.format(DomainErrorMessages.ONLY_PROCESSING_CAN_COMPLETE, transfer.getStatus())
            );
        }
        transfer.markAsCompleted();
    }

    @Override
    public boolean isApplicable(Transfer transfer) {
        return transfer.getStatus() == TransferStatus.PROCESSING;
    }
}


