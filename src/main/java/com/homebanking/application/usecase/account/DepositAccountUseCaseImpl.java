package com.homebanking.application.usecase.account;

import com.homebanking.application.dto.account.request.DepositAccountInputRequest;
import com.homebanking.application.dto.account.response.DepositAccountOutputResponse;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.exception.account.AccountNotFoundException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.account.DepositAccountInputPort;
import com.homebanking.port.out.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class DepositAccountUseCaseImpl implements DepositAccountInputPort {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public DepositAccountOutputResponse deposit(DepositAccountInputRequest request) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        DomainErrorMessages.ACCOUNT_NOT_FOUND,
                        request.accountId()
                ));

        account.deposit(request.amount());
        Account saved = accountRepository.save(account);

        return new DepositAccountOutputResponse(
                saved.getId(),
                saved.getBalance().value()
        );
    }
}
