package com.homebanking.application.usecase.payment;

import com.homebanking.application.dto.payment.request.PayBillInputRequest;
import com.homebanking.application.dto.payment.response.BillPaymentOutputResponse;
import com.homebanking.application.mapper.BillPaymentMapper;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.BillPayment;
import com.homebanking.domain.entity.User;
import com.homebanking.port.out.account.AccountRepository;
import com.homebanking.port.out.payment.BillPaymentRepository;
import com.homebanking.port.out.payment.BillProcessorOutputPort;
import com.homebanking.port.out.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayBillUseCaseImplTest {

    @Mock
    private BillPaymentRepository billPaymentRepository;
    @Mock
    private BillProcessorOutputPort billProcessorOutputPort;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;

    private PayBillUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new PayBillUseCaseImpl(
                billPaymentRepository,
                billProcessorOutputPort,
                accountRepository,
                userRepository,
                new BillPaymentMapper()
        );
    }

    @Test
    void shouldPayBillAndDebitAccount() {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PayBillInputRequest request = new PayBillInputRequest(
                accountId,
                "EDENOR",
                "INV-123",
                new BigDecimal("1500.00"),
                "idem-1",
                "user@test.com"
        );

        Account account = Account.withId(
                accountId,
                userId,
                "1234567890123456789012",
                "usuario.test",
                new BigDecimal("5000.00"),
                LocalDateTime.now()
        );
        User user = User.withId(
                userId,
                "user@test.com",
                "Password123!",
                "Test",
                "User",
                "30111222",
                LocalDate.of(1990, 1, 1),
                "Street 123",
                LocalDateTime.now()
        );

        when(billPaymentRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.empty());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(billPaymentRepository.save(any(BillPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(billProcessorOutputPort.process(any(BillPayment.class))).thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BillPaymentOutputResponse output = useCase.pay(request);

        assertThat(output.status()).isEqualTo("PAID");
        assertThat(output.failureReason()).isNull();
    }
}

