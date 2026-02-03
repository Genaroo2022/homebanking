package com.homebanking.application.usecase.auth;

import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.authentication.StartTotpSetupInputPort;
import com.homebanking.port.out.auth.TotpService;
import com.homebanking.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
public class StartTotpSetupUseCaseImpl implements StartTotpSetupInputPort {

    private final UserRepository userRepository;
    private final TotpService totpService;

    @Override
    public void startSetup(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidUserDataException(DomainErrorMessages.USER_NOT_FOUND));

        String secret = totpService.generateSecret();
        user.startTotpSetup(secret);
        userRepository.save(user);
    }
}
