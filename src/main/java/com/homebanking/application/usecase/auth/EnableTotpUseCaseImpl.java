package com.homebanking.application.usecase.auth;

import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.authentication.EnableTotpInputPort;
import com.homebanking.port.out.auth.TotpService;
import com.homebanking.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
public class EnableTotpUseCaseImpl implements EnableTotpInputPort {

    private final UserRepository userRepository;
    private final TotpService totpService;

    @Override
    public void enable(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidUserDataException(DomainErrorMessages.USER_NOT_FOUND));

        if (user.getTotpSecret() == null) {
            throw new InvalidUserDataException(DomainErrorMessages.TOTP_SECRET_REQUIRED);
        }
        if (code == null || code.isBlank()) {
            throw new InvalidUserDataException(DomainErrorMessages.TOTP_CODE_REQUIRED);
        }
        if (!totpService.verifyCode(user.getTotpSecret().value(), code)) {
            throw new InvalidUserDataException(DomainErrorMessages.TOTP_CODE_INVALID);
        }

        user.enableTotp();
        userRepository.save(user);
    }
}
