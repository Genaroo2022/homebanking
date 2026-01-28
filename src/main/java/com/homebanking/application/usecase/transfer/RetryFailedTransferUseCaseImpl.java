package com.homebanking.application.usecase.transfer;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.exception.transfer.TransferNotFoundException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.out.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class RetryFailedTransferUseCaseImpl implements RetryFailedTransferUseCase {

    private final TransferRepository transferRepository;
    private final ProcessTransferUseCase processTransferUseCase;

    @Override
    @Transactional
    public TransferOutputResponse retryFailedTransfer(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException(
                        DomainErrorMessages.TRANSFER_NOT_FOUND,
                        transferId
                ));

        if (!transfer.isRetryable()) {
            throw new InvalidTransferDataException(
                    String.format(DomainErrorMessages.ONLY_FAILED_CAN_RETRY, transfer.getStatus())
            );
        }

        return processTransferUseCase.processTransfer(transferId);
    }
}
