package com.homebanking.application.usecase;

import com.homebanking.application.dto.response.UserProfileOutput;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetUserProfileUseCase {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public UserProfileOutput getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidUserDataException(DomainErrorMessages.USER_NOT_FOUND));

        List<Account> accounts = accountRepository.findByUserId(user.getId());

        List<UserProfileOutput.AccountOutput> accountOutputs = accounts.stream()
                .map(acc -> new UserProfileOutput.AccountOutput(
                        acc.getId(), acc.getCbu(), acc.getAlias(), acc.getBalance()))
                .toList();

        return new UserProfileOutput(
                user.getId(), user.getEmail(), user.getName(), user.getLastName(), accountOutputs
        );
    }
}