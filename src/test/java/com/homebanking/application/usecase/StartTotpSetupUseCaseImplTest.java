package com.homebanking.application.usecase;

import com.homebanking.application.usecase.auth.StartTotpSetupUseCaseImpl;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.port.out.auth.TotpService;
import com.homebanking.port.out.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartTotpSetupUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TotpService totpService;

    private StartTotpSetupUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new StartTotpSetupUseCaseImpl(userRepository, totpService);
    }

    @Test
    void shouldStoreSecretWhenUserExists() {
        String email = "user@example.com";
        User user = createTestUser(email, "hashed_pwd");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(totpService.generateSecret()).thenReturn("JBSWY3DPEHPK3PXP");

        useCase.startSetup(email);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getTotpSecret().value()).isEqualTo("JBSWY3DPEHPK3PXP");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.startSetup("missing@example.com"))
                .isInstanceOf(InvalidUserDataException.class);
    }

    private User createTestUser(String email, String hashedPassword) {
        return User.withId(
                UUID.randomUUID(),
                email,
                hashedPassword,
                "John",
                "Doe",
                "12345678",
                LocalDate.of(1990, 1, 1),
                "123 Street",
                LocalDateTime.now()
        );
    }
}
