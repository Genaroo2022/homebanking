package com.homebanking.domain.service.transfer;

import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.policy.transfer.TransferStateTransition;
import com.homebanking.domain.policy.transfer.TransferStateTransitionFactory;
import com.homebanking.domain.util.DomainErrorMessages;

public class TransferStateService {
    private final TransferStateTransitionFactory factory;

    public TransferStateService(TransferStateTransitionFactory factory) {
        this.factory = factory;
    }

    /**
     * Aplica una transición usando el patrón Strategy.
     * Es un SERVICIO porque orquesta políticas.
     */
    public void applyTransition(Transfer transfer, TransferStateTransition.Type type) {
        TransferStateTransition transition = factory.create(type);

        if (!transition.isApplicable(transfer)) {
            throw new InvalidTransferDataException(
                    String.format(DomainErrorMessages.TRANSFER_INCONSISTENT_STATE, transfer.getStatus())
            );
        }

        transition.execute(transfer);
    }
}
