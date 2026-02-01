package com.homebanking.application.usecase.transfer;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.application.dto.transfer.response.TransferProcessingResult;
import com.homebanking.application.mapper.TransferMapper;
import com.homebanking.application.service.transfer.TransferStateTransitionService;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.transfer.TransferProcessingException;
import com.homebanking.port.in.transfer.ProcessTransferInputPort;
import com.homebanking.port.out.transfer.TransferProcessorOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class ProcessTransferUseCaseImpl implements ProcessTransferInputPort {

    private final TransferProcessorOutputPort transferProcessor;
    private final TransferMapper transferMapper;
    private final TransferStateTransitionService stateService;

    @Override
    public TransferOutputResponse processTransfer(UUID transferId) {
        // 1) Prepare transfer in a short, atomic transaction.
        Transfer transferToProcess = stateService.prepareForProcessing(transferId);

        // 2) External call outside of any DB transaction.
        TransferProcessingResult result = executeExternalProcessing(transferToProcess);

        // 3) Persist final result in a new transaction.
        Transfer finalizedTransfer = stateService.finalizeProcessing(transferToProcess.getId(), result);

        // 4) Map to DTO.
        return transferMapper.toDto(finalizedTransfer);
    }

    private TransferProcessingResult executeExternalProcessing(Transfer transfer) {
        try {
            boolean processedSuccessfully = transferProcessor.processTransfer(transfer);
            return mapToResult(processedSuccessfully, null);
        } catch (TransferProcessingException ex) {
            return mapToResult(false, ex);
        }
    }

    private TransferProcessingResult mapToResult(boolean success, TransferProcessingException error) {
        if (success) {
            return TransferProcessingResult.success();
        }
        if (error != null && !error.isRecoverable()) {
            return TransferProcessingResult.nonRecoverableFailure(error.getMessage());
        }
        return TransferProcessingResult.recoverableFailure();
    }
}



