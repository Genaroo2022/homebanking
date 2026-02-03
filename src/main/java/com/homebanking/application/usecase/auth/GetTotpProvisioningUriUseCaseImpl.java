package com.homebanking.application.usecase.auth;

import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.authentication.GetTotpProvisioningUriInputPort;
import com.homebanking.port.out.auth.TotpService;
import com.homebanking.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTotpProvisioningUriUseCaseImpl implements GetTotpProvisioningUriInputPort {

    private final UserRepository userRepository;
    private final TotpService totpService;

    @Value("${totp.issuer:HomeBanking}")
    private String issuer;

    @Override
    public String getProvisioningUri(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidUserDataException(DomainErrorMessages.USER_NOT_FOUND));

        if (user.getTotpSecret() == null) {
            throw new InvalidUserDataException(DomainErrorMessages.TOTP_SECRET_REQUIRED);
        }

        return totpService.buildProvisioningUri(issuer, user.getEmail().value(), user.getTotpSecret().value());
    }
}
