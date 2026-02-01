package com.homebanking.application.service.transfer;

import com.homebanking.application.dto.transfer.response.TransferProcessingResult;
import com.homebanking.application.service.transfer.action.TransferProcessingAction;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.account.InvalidAccountDataException;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.exception.transfer.TransferNotFoundException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferStateTransitionService {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final List<TransferProcessingAction> actions;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transfer prepareForProcessing(UUID transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException(DomainErrorMessages.TRANSFER_NOT_FOUND, transferId));

        if (!transfer.isEligibleForProcessing()) {
            throw new InvalidTransferDataException(
                    String.format(DomainErrorMessages.INVALID_PROCESSING_TRANSITION, transfer.getStatus()));
        }

        if (!accountRepository.existsByCbu(transfer.getTargetCbu())) {
            transfer.markAsProcessing();
            transfer.markAsRejected(DomainErrorMessages.ACCOUNT_NOT_FOUND);
            transferRepository.save(transfer);
            throw new InvalidAccountDataException(DomainErrorMessages.ACCOUNT_NOT_FOUND);
        }

        transfer.markAsProcessing();
        return transferRepository.save(transfer);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transfer finalizeProcessing(UUID transferId, TransferProcessingResult result) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException(DomainErrorMessages.TRANSFER_NOT_FOUND, transferId));

        Map<TransferProcessingResult.Outcome, TransferProcessingAction> actionMap = buildActionMap();
        TransferProcessingAction action = actionMap.get(result.outcome());
        if (action == null) {
            throw new InvalidTransferDataException("Resultado de procesamiento invalido: " + result.outcome());
        }

        action.apply(transfer, result);
        return transferRepository.save(transfer);
    }

    private Map<TransferProcessingResult.Outcome, TransferProcessingAction> buildActionMap() {
        Map<TransferProcessingResult.Outcome, TransferProcessingAction> map =
                new EnumMap<>(TransferProcessingResult.Outcome.class);
        for (TransferProcessingAction action : actions) {
            map.put(action.outcome(), action);
        }
        return map;
    }
}

