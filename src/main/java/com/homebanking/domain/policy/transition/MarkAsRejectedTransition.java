package com.homebanking.domain.policy.transition;

import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.enums.TransferStatus;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.policy.transfer.TransferStateTransition;
import com.homebanking.domain.util.DomainErrorMessages;

public class MarkAsRejectedTransition implements TransferStateTransition {
    @Override
    public void execute(Transfer transfer) {
        if (!isApplicable(transfer)) {
            throw new InvalidTransferDataException(
                    String.format(DomainErrorMessages.CANNOT_REJECT_TRANSFER, transfer.getStatus())
            );
        }
        transfer.markAsRejected("Rechazo automatico.");
    }

    @Override
    public boolean isApplicable(Transfer transfer) {
        if (transfer.getStatus() == TransferStatus.PROCESSING) {
            return true;
        }
        return transfer.getStatus() == TransferStatus.FAILED && !transfer.isRetryable();
    }
}
