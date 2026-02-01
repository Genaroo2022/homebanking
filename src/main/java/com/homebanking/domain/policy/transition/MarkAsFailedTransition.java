package com.homebanking.domain.policy.transition;

import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.enums.TransferStatus;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.policy.transfer.TransferStateTransition;
import com.homebanking.domain.util.DomainErrorMessages;

public class MarkAsFailedTransition implements TransferStateTransition {
    private final String reason;

    public MarkAsFailedTransition(String reason) {
        this.reason = reason;
    }

    @Override
    public void execute(Transfer transfer) {
        if (!isApplicable(transfer)) {
            throw new InvalidTransferDataException(
                    String.format(DomainErrorMessages.ONLY_PROCESSING_CAN_FAIL, transfer.getStatus())
            );
        }
        transfer.markAsFailed(reason);
    }

    @Override
    public boolean isApplicable(Transfer transfer) {
        return transfer.getStatus() == TransferStatus.PROCESSING;
    }
}


