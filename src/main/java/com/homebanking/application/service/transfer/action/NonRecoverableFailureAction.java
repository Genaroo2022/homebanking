package com.homebanking.application.service.transfer.action;

import com.homebanking.application.dto.transfer.response.TransferProcessingResult;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.account.InvalidAccountDataException;
import com.homebanking.domain.policy.transition.MarkAsRejectedTransition;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.out.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NonRecoverableFailureAction implements TransferProcessingAction {

    private final AccountRepository accountRepository;

    @Override
    public TransferProcessingResult.Outcome outcome() {
        return TransferProcessingResult.Outcome.NON_RECOVERABLE_FAILURE;
    }

    @Override
    public void apply(Transfer transfer, TransferProcessingResult result) {
        String reason = result.errorMessage()
                .orElse("Error no recuperable durante procesamiento.");
        new MarkAsRejectedTransition(reason).execute(transfer);

        Account originAccount = accountRepository.findById(transfer.getOriginAccountId())
                .orElseThrow(() -> new InvalidAccountDataException(DomainErrorMessages.ACCOUNT_NOT_FOUND));
        originAccount.deposit(transfer.getAmount().value());
        accountRepository.save(originAccount);
    }
}


