package com.homebanking.application.service.transfer.action;

import com.homebanking.application.dto.transfer.response.TransferProcessingResult;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.account.InvalidAccountDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.out.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompleteTransferAction implements TransferProcessingAction {

    private final AccountRepository accountRepository;

    @Override
    public TransferProcessingResult.Outcome outcome() {
        return TransferProcessingResult.Outcome.SUCCESS;
    }

    @Override
    public void apply(Transfer transfer, TransferProcessingResult result) {
        transfer.markAsCompleted();
        Account destinationAccount = accountRepository.findByCbu(transfer.getTargetCbu())
                .orElseThrow(() -> new InvalidAccountDataException(DomainErrorMessages.ACCOUNT_NOT_FOUND));
        destinationAccount.deposit(transfer.getAmount().value());
        accountRepository.save(destinationAccount);
    }
}
