package com.homebanking.application.service.auth;

import com.homebanking.domain.exception.user.TooManyLoginAttemptsException;
import com.homebanking.domain.model.LoginAttempt;
import com.homebanking.port.out.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long BASE_DELAY_SECONDS = 1;

    public void checkLoginAllowed(String username) {
        List<LoginAttempt> failedAttempts = loginAttemptRepository.findRecentFailedAttempts(username);
        if (failedAttempts.isEmpty()) {
            return;
        }

        int consecutiveFailures = failedAttempts.size();

        if (consecutiveFailures >= MAX_FAILED_ATTEMPTS) {
            LoginAttempt lastAttempt = failedAttempts.get(0); // Most recent is first
            long delay = (long) (BASE_DELAY_SECONDS * Math.pow(2, consecutiveFailures - MAX_FAILED_ATTEMPTS));
            long secondsSinceLastAttempt = Duration.between(lastAttempt.timestamp(), LocalDateTime.now()).getSeconds();

            if (secondsSinceLastAttempt < delay) {
                long retryAfter = delay - secondsSinceLastAttempt;
                throw new TooManyLoginAttemptsException("Too many failed login attempts. Please try again later.", retryAfter);
            }
        }
    }

    public void handleFailedAttempt(String username, String ipAddress) {
        LoginAttempt attempt = new LoginAttempt(username, ipAddress, LocalDateTime.now(), false);
        loginAttemptRepository.save(attempt);
    }

    public void handleSuccessfulAttempt(String username, String ipAddress) {
        LoginAttempt attempt = new LoginAttempt(username, ipAddress, LocalDateTime.now(), true);
        loginAttemptRepository.save(attempt);
    }
}
