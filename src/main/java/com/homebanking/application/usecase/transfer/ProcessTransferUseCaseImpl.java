package com.homebanking.application.usecase.transfer;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.exception.transfer.TransferNotFoundException;
import com.homebanking.domain.exception.transfer.TransferProcessingException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.NotificationOutputPort;
import com.homebanking.port.out.TransferProcessorOutputPort;
import com.homebanking.port.out.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Slf4j
public class ProcessTransferUseCaseImpl implements ProcessTransferUseCase {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final TransferProcessorOutputPort transferProcessor;
    private final NotificationOutputPort notificationPort;

    @Override
    @Transactional
    public TransferOutputResponse processTransfer(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException(
                        DomainErrorMessages.TRANSFER_NOT_FOUND,
                        transferId
                ));

        if (!transfer.isEligibleForProcessing()) {
            throw new InvalidTransferDataException(
                    String.format(DomainErrorMessages.INVALID_PROCESSING_TRANSITION, transfer.getStatus())
            );
        }

        transfer.markAsProcessing();
        transferRepository.save(transfer);

        Account destinationAccount = accountRepository.findByCbu(transfer.getTargetCbu().value())
                .orElse(null);
        if (destinationAccount == null) {
            transfer.markAsRejected(DomainErrorMessages.ACCOUNT_NOT_FOUND);
            Transfer saved = transferRepository.save(transfer);
            notificationPort.notifyTransferFailed(saved);
            return toOutputResponse(saved);
        }

        try {
            boolean processed = transferProcessor.processTransfer(transfer);
            if (processed) {
                transfer.markAsCompleted();
                Transfer saved = transferRepository.save(transfer);
                destinationAccount.deposit(saved.getAmount().value());
                accountRepository.save(destinationAccount);
                notificationPort.notifyTransferCompleted(saved);
                return toOutputResponse(saved);
            }

            handleRetryableFailure(transfer);
            Transfer saved = transferRepository.save(transfer);
            return toOutputResponse(saved);

        } catch (TransferProcessingException ex) {
            if (ex.isRecoverable()) {
                handleRetryableFailure(transfer);
                Transfer saved = transferRepository.save(transfer);
                return toOutputResponse(saved);
            }

            transfer.markAsRejected(ex.getMessage());
            Transfer saved = transferRepository.save(transfer);
            notificationPort.notifyTransferFailed(saved);
            return toOutputResponse(saved);
        }
    }

    private void handleRetryableFailure(Transfer transfer) {
        transfer.markAsFailed("Error temporal durante procesamiento. Se reintentara automaticamente.");
        log.warn("Transferencia marcada para reintento: id={}, attempt={}",
                transfer.getId(), transfer.getRetryCount());
    }

    private TransferOutputResponse toOutputResponse(Transfer transfer) {
        return new TransferOutputResponse(
                transfer.getId(),
                transfer.getIdempotencyKey().value(),
                transfer.getOriginAccountId(),
                transfer.getTargetCbu().value(),
                transfer.getAmount().value(),
                transfer.getDescription().value(),
                transfer.getStatus().name(),
                transfer.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
