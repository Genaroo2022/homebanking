package com.homebanking.application.usecase.transfer;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.account.AccountNotFoundException;
import com.homebanking.domain.exception.security.AccessDeniedException;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.exception.transfer.TransferNotFoundException;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.transfer.ProcessTransferInputPort;
import com.homebanking.port.in.transfer.RetryTransferInputPort;
import com.homebanking.port.out.account.AccountRepository;
import com.homebanking.port.out.transfer.TransferRepository;
import com.homebanking.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
public class RetryFailedTransferUseCaseImpl implements RetryTransferInputPort {

    private final TransferRepository transferRepository;
    private final ProcessTransferInputPort processTransferUseCase;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public TransferOutputResponse retryFailedTransfer(UUID transferId) {
        return retryFailedTransfer(transferId, null);
    }

    @Override
    @Transactional
    public TransferOutputResponse retryFailedTransfer(UUID transferId, String requesterEmail) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException(
                        DomainErrorMessages.TRANSFER_NOT_FOUND,
                        transferId
                ));

        if (requesterEmail != null && !requesterEmail.isBlank()) {
            validateOwnership(transfer, requesterEmail);
        }

        if (!transfer.isRetryable()) {
            throw new InvalidTransferDataException(
                    String.format(DomainErrorMessages.ONLY_FAILED_CAN_RETRY, transfer.getStatus())
            );
        }

        return processTransferUseCase.processTransfer(transferId);
    }

    private void validateOwnership(Transfer transfer, String requesterEmail) {
        User user = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new InvalidUserDataException(
                        DomainErrorMessages.USER_NOT_FOUND));

        Account originAccount = accountRepository.findById(transfer.getOriginAccountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        DomainErrorMessages.ACCOUNT_NOT_FOUND,
                        transfer.getOriginAccountId()
                ));

        if (!originAccount.getUserId().equals(user.getId())) {
            throw new AccessDeniedException(DomainErrorMessages.ACCESS_DENIED);
        }
    }
}


