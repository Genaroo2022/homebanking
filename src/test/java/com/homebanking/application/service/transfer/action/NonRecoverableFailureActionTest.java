package com.homebanking.application.service.transfer.action;

import com.homebanking.application.dto.transfer.response.TransferProcessingResult;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.enums.TransferStatus;
import com.homebanking.domain.exception.account.InvalidAccountDataException;
import com.homebanking.domain.valueobject.common.Cbu;
import com.homebanking.domain.valueobject.transfer.IdempotencyKey;
import com.homebanking.domain.valueobject.transfer.TransferAmount;
import com.homebanking.domain.valueobject.transfer.TransferDescription;
import com.homebanking.port.out.account.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NonRecoverableFailureActionTest {

    @Mock
    private AccountRepository accountRepository;

    private NonRecoverableFailureAction action;

    @BeforeEach
    void setUp() {
        action = new NonRecoverableFailureAction(accountRepository);
    }

    @Test
    void shouldCompensateOriginAccountAndRejectTransfer() {
        UUID accountId = UUID.randomUUID();
        Transfer transfer = createTransfer(TransferStatus.PROCESSING, accountId, new BigDecimal("100"));
        Account origin = createAccount(accountId, new BigDecimal("50.00"));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(origin));

        action.apply(transfer, TransferProcessingResult.nonRecoverableFailure("err"));

        assertThat(transfer.getStatus()).isEqualTo(TransferStatus.REJECTED);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance().value()).isEqualByComparingTo("150.00");
    }

    @Test
    void shouldThrowWhenOriginAccountMissing() {
        UUID accountId = UUID.randomUUID();
        Transfer transfer = createTransfer(TransferStatus.PROCESSING, accountId, new BigDecimal("100"));

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> action.apply(transfer, TransferProcessingResult.nonRecoverableFailure("err")))
                .isInstanceOf(InvalidAccountDataException.class);
    }

    private Transfer createTransfer(TransferStatus status, UUID originAccountId, BigDecimal amount) {
        return Transfer.reconstruct(
                UUID.randomUUID(),
                IdempotencyKey.of("idem-key"),
                originAccountId,
                Cbu.of("1234567890123456789012"),
                TransferAmount.of(amount),
                TransferDescription.of("Test"),
                status,
                LocalDateTime.now(),
                null,
                null,
                null,
                0,
                null
        );
    }

    private Account createAccount(UUID accountId, BigDecimal balance) {
        return Account.withId(
                accountId,
                UUID.randomUUID(),
                "1234567890123456789012",
                "alias.test",
                balance,
                LocalDateTime.now()
        );
    }
}
