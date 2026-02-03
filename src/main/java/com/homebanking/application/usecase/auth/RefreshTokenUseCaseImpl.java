package com.homebanking.application.usecase.auth;

import com.homebanking.application.dto.authentication.request.RefreshTokenInputRequest;
import com.homebanking.application.dto.authentication.response.TokenOutputResponse;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.authentication.RefreshTokenInputPort;
import com.homebanking.port.out.auth.RefreshTokenService;
import com.homebanking.port.out.auth.RefreshTokenStore;
import com.homebanking.port.out.auth.TokenGenerator;
import com.homebanking.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenUseCaseImpl implements RefreshTokenInputPort {

    private final RefreshTokenService refreshTokenService;
    private final TokenGenerator tokenGenerator;
    private final UserRepository userRepository;
    private final RefreshTokenStore refreshTokenStore;

    @Override
    public TokenOutputResponse refresh(RefreshTokenInputRequest request) {
        if (!refreshTokenService.isRefreshTokenValid(request.refreshToken())) {
            throw new InvalidUserDataException(DomainErrorMessages.INVALID_REFRESH_TOKEN);
        }

        if (refreshTokenStore.isBlacklisted(request.refreshToken())) {
            throw new InvalidUserDataException(DomainErrorMessages.INVALID_REFRESH_TOKEN);
        }

        String subject = refreshTokenService.extractSubject(request.refreshToken());
        if (subject == null || subject.isBlank()) {
            throw new InvalidUserDataException(DomainErrorMessages.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findByEmail(subject)
                .orElseThrow(() -> new InvalidUserDataException(
                        DomainErrorMessages.USER_NOT_FOUND));

        String accessToken = tokenGenerator.generateToken(user.getEmail().value());
        String refreshToken = refreshTokenService.generateRefreshToken(user.getEmail().value());

        refreshTokenStore.blacklist(
                request.refreshToken(),
                refreshTokenService.getExpirationMillis(request.refreshToken())
        );

        return new TokenOutputResponse(accessToken, refreshToken);
    }
}
