package com.homebanking.application.usecase;

import com.homebanking.application.usecase.auth.GetTotpProvisioningUriUseCaseImpl;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.port.out.auth.TotpService;
import com.homebanking.port.out.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTotpProvisioningUriUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TotpService totpService;

    private GetTotpProvisioningUriUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetTotpProvisioningUriUseCaseImpl(userRepository, totpService);
        ReflectionTestUtils.setField(useCase, "issuer", "HomeBanking");
    }

    @Test
    void shouldReturnProvisioningUriWhenSecretExists() {
        String email = "user@example.com";
        User user = createTestUserWithSecret(email, "hashed_pwd", "JBSWY3DPEHPK3PXP");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(totpService.buildProvisioningUri("HomeBanking", email, "JBSWY3DPEHPK3PXP"))
                .thenReturn("otpauth://totp/HomeBanking:user@example.com?secret=JBSWY3DPEHPK3PXP");

        String uri = useCase.getProvisioningUri(email);

        assertThat(uri).contains("otpauth://totp");
    }

    @Test
    void shouldThrowWhenSecretMissing() {
        String email = "user@example.com";
        User user = createTestUser(email, "hashed_pwd");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> useCase.getProvisioningUri(email))
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

    private User createTestUserWithSecret(String email, String hashedPassword, String secret) {
        return User.withId(
                UUID.randomUUID(),
                email,
                hashedPassword,
                "John",
                "Doe",
                "12345678",
                LocalDate.of(1990, 1, 1),
                "123 Street",
                LocalDateTime.now(),
                secret,
                false
        );
    }
}
