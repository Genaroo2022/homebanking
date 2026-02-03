package com.homebanking.application.usecase.transfer;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.application.mapper.TransferMapper;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.account.AccountNotFoundException;
import com.homebanking.domain.exception.security.AccessDeniedException;
import com.homebanking.domain.exception.transfer.TransferNotFoundException;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.transfer.GetTransferInputPort;
import com.homebanking.port.out.account.AccountRepository;
import com.homebanking.port.out.transfer.TransferRepository;
import com.homebanking.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@RequiredArgsConstructor
public class GetTransferUseCaseImpl implements GetTransferInputPort {

    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Override
    public TransferOutputResponse getTransfer(UUID transferId, String requesterEmail) {
        if (requesterEmail == null || requesterEmail.isBlank()) {
            throw new InvalidUserDataException(DomainErrorMessages.USER_NOT_FOUND);
        }

        User user = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new InvalidUserDataException(
                        DomainErrorMessages.USER_NOT_FOUND));

        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException(
                        DomainErrorMessages.TRANSFER_NOT_FOUND,
                        transferId
                ));
        Account originAccount = accountRepository.findById(transfer.getOriginAccountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        DomainErrorMessages.ACCOUNT_NOT_FOUND,
                        transfer.getOriginAccountId()
                ));

        if (!originAccount.getUserId().equals(user.getId())) {
            throw new AccessDeniedException(DomainErrorMessages.ACCESS_DENIED);
        }

        return transferMapper.toDto(transfer);
    }
}


