package com.homebanking.application.usecase.account;

import com.homebanking.application.dto.account.request.DepositAccountInputRequest;
import com.homebanking.application.dto.account.response.DepositAccountOutputResponse;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.account.AccountNotFoundException;
import com.homebanking.domain.exception.security.AccessDeniedException;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.account.DepositAccountInputPort;
import com.homebanking.port.out.account.AccountRepository;
import com.homebanking.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class DepositAccountUseCaseImpl implements DepositAccountInputPort {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DepositAccountOutputResponse deposit(DepositAccountInputRequest request) {
        User user = userRepository.findByEmail(request.requesterEmail())
                .orElseThrow(() -> new InvalidUserDataException(
                        DomainErrorMessages.USER_NOT_FOUND));

        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        DomainErrorMessages.ACCOUNT_NOT_FOUND,
                        request.accountId()
                ));

        if (!account.getUserId().equals(user.getId())) {
            throw new AccessDeniedException(DomainErrorMessages.ACCESS_DENIED);
        }

        account.deposit(request.amount());
        Account saved = accountRepository.save(account);

        return new DepositAccountOutputResponse(
                saved.getId(),
                saved.getBalance().value()
        );
    }
}


