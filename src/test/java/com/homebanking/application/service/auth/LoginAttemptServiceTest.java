package com.homebanking.application.service.auth;

import com.homebanking.domain.exception.user.TooManyLoginAttemptsException;
import com.homebanking.domain.model.LoginAttempt;
import com.homebanking.port.out.security.LoginAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private LoginAttemptRepository loginAttemptRepository;

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService(loginAttemptRepository);
    }

    @Test
    void shouldAllowLogin_WhenNoFailedAttempts() {
        String username = "user@example.com";
        when(loginAttemptRepository.findRecentFailedAttempts(username))
                .thenReturn(List.of());

        assertThatCode(() -> loginAttemptService.checkLoginAllowed(username))
                .doesNotThrowAnyException();

        verify(loginAttemptRepository).findRecentFailedAttempts(username);
    }

    @Test
    void shouldAllowLogin_WhenFailuresBelowThreshold() {
        String username = "user@example.com";
        List<LoginAttempt> attempts = List.of(
                new LoginAttempt(username, "10.0.0.1", LocalDateTime.now(), false),
                new LoginAttempt(username, "10.0.0.1", LocalDateTime.now(), false),
                new LoginAttempt(username, "10.0.0.1", LocalDateTime.now(), false),
                new LoginAttempt(username, "10.0.0.1", LocalDateTime.now(), false)
        );
        when(loginAttemptRepository.findRecentFailedAttempts(username))
                .thenReturn(attempts);

        assertThatCode(() -> loginAttemptService.checkLoginAllowed(username))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrow_WhenTooManyAttemptsAndDelayNotElapsed() {
        String username = "user@example.com";
        LocalDateTime lastAttemptTime = LocalDateTime.now();
        List<LoginAttempt> attempts = List.of(
                new LoginAttempt(username, "10.0.0.1", lastAttemptTime, false),
                new LoginAttempt(username, "10.0.0.1", lastAttemptTime.minusSeconds(1), false),
                new LoginAttempt(username, "10.0.0.1", lastAttemptTime.minusSeconds(2), false),
                new LoginAttempt(username, "10.0.0.1", lastAttemptTime.minusSeconds(3), false),
                new LoginAttempt(username, "10.0.0.1", lastAttemptTime.minusSeconds(4), false),
                new LoginAttempt(username, "10.0.0.1", lastAttemptTime.minusSeconds(5), false)
        );
        when(loginAttemptRepository.findRecentFailedAttempts(username))
                .thenReturn(attempts);

        assertThatThrownBy(() -> loginAttemptService.checkLoginAllowed(username))
                .isInstanceOf(TooManyLoginAttemptsException.class)
                .satisfies(ex -> {
                    TooManyLoginAttemptsException typed = (TooManyLoginAttemptsException) ex;
                    assertThat(typed.getRetryAfterSeconds()).isBetween(1L, 2L);
                });
    }

    @Test
    void shouldAllowLogin_WhenDelayElapsed() {
        String username = "user@example.com";
        LocalDateTime lastAttemptTime = LocalDateTime.now().minusSeconds(5);
        List<LoginAttempt> attempts = List.of(
                new LoginAttempt(username, "10.0.0.1", lastAttemptTime, false),
                new LoginAttempt(username, "10.0.0.1", lastAttemptTime.minusSeconds(1), false),
                new LoginAttempt(username, "10.0.0.1", lastAttemptTime.minusSeconds(2), false),
                new LoginAttempt(username, "10.0.0.1", lastAttemptTime.minusSeconds(3), false),
                new LoginAttempt(username, "10.0.0.1", lastAttemptTime.minusSeconds(4), false),
                new LoginAttempt(username, "10.0.0.1", lastAttemptTime.minusSeconds(5), false)
        );
        when(loginAttemptRepository.findRecentFailedAttempts(username))
                .thenReturn(attempts);

        assertThatCode(() -> loginAttemptService.checkLoginAllowed(username))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldSaveFailedAttempt() {
        String username = "user@example.com";
        String ipAddress = "10.0.0.9";

        loginAttemptService.handleFailedAttempt(username, ipAddress);

        ArgumentCaptor<LoginAttempt> captor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(loginAttemptRepository).save(captor.capture());
        LoginAttempt attempt = captor.getValue();
        assertThat(attempt.username()).isEqualTo(username);
        assertThat(attempt.ipAddress()).isEqualTo(ipAddress);
        assertThat(attempt.successful()).isFalse();
        assertThat(attempt.timestamp()).isNotNull();
    }

    @Test
    void shouldSaveSuccessfulAttempt() {
        String username = "user@example.com";
        String ipAddress = "10.0.0.10";

        loginAttemptService.handleSuccessfulAttempt(username, ipAddress);

        ArgumentCaptor<LoginAttempt> captor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(loginAttemptRepository).save(captor.capture());
        LoginAttempt attempt = captor.getValue();
        assertThat(attempt.username()).isEqualTo(username);
        assertThat(attempt.ipAddress()).isEqualTo(ipAddress);
        assertThat(attempt.successful()).isTrue();
        assertThat(attempt.timestamp()).isNotNull();
    }
}


