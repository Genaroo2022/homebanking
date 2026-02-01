package com.homebanking.application.usecase;

import com.homebanking.application.dto.transfer.request.CreateTransferInputRequest;
import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.application.usecase.transfer.CreateTransferUseCaseImpl;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.event.TransferCreatedEvent;
import com.homebanking.domain.exception.account.AccountNotFoundException;
import com.homebanking.domain.exception.account.InsufficientFundsException;
import com.homebanking.domain.exception.account.InvalidAccountDataException;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.exception.transfer.SameAccountTransferException;
import com.homebanking.domain.valueobject.common.Cbu;
import com.homebanking.domain.valueobject.transfer.IdempotencyKey;
import com.homebanking.domain.valueobject.transfer.TransferAmount;
import com.homebanking.domain.valueobject.transfer.TransferDescription;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.EventPublisher;
import com.homebanking.port.out.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTransferUseCaseImplTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ORIGIN_ACCOUNT_ID = UUID.randomUUID();
    private static final UUID TRANSFER_ID = UUID.randomUUID();

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private EventPublisher eventPublisher;

    private CreateTransferUseCaseImpl createTransferUseCase;

    @BeforeEach
    void setUp() {
        createTransferUseCase = new CreateTransferUseCaseImpl(
                accountRepository,
                transferRepository,
                eventPublisher
        );
    }

    @Test
    void shouldCreateTransferSuccessfully_WhenDomainLogicSucceeds() {
        // Arrange
        String targetCbuStr = "1234567890123456789012";
        Cbu targetCbu = Cbu.of(targetCbuStr);
        BigDecimal amount = new BigDecimal("100.00");
        String idempotencyKey = UUID.randomUUID().toString();
        CreateTransferInputRequest request = new CreateTransferInputRequest(ORIGIN_ACCOUNT_ID, targetCbuStr, amount, "Pago", idempotencyKey);

        Account originAccount = spy(createTestAccount(ORIGIN_ACCOUNT_ID, "1111111111111111111111", "alias1", new BigDecimal("500")));
        Transfer createdTransfer = createTestTransfer(TRANSFER_ID, request);

        when(transferRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(accountRepository.existsByCbu(targetCbu)).thenReturn(true);
        when(accountRepository.findById(ORIGIN_ACCOUNT_ID)).thenReturn(Optional.of(originAccount));
        doReturn(createdTransfer).when(originAccount).initiateTransferTo(any(), any(), any(), any());

        // Act
        TransferOutputResponse result = createTransferUseCase.createTransfer(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(TRANSFER_ID);
        assertThat(result.status()).isEqualTo("PENDING");

        // Verify
        verify(originAccount).initiateTransferTo(
                eq(targetCbu),
                argThat(a -> a.value().equals(amount)),
                argThat(d -> d.value().equals("Pago")),
                argThat(i -> i.value().equals(idempotencyKey))
        );
        verify(accountRepository).save(originAccount);
        verify(transferRepository).save(createdTransfer);
        verify(eventPublisher).publish(any(TransferCreatedEvent.class));
    }

    @Test
    void shouldReturnExistingTransfer_WhenIdempotencyKeyAlreadyExists() {
        // Arrange
        String idempotencyKey = UUID.randomUUID().toString();
        CreateTransferInputRequest request = new CreateTransferInputRequest(ORIGIN_ACCOUNT_ID, "1234567890123456789012", new BigDecimal("100.00"), "Pago", idempotencyKey);
        Transfer existingTransfer = createTestTransfer(TRANSFER_ID, request);
        when(transferRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingTransfer));

        // Act
        TransferOutputResponse result = createTransferUseCase.createTransfer(request);

        // Assert
        assertThat(result.idempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(result.id()).isEqualTo(TRANSFER_ID);

        // Verify
        verify(accountRepository, never()).findById(any(UUID.class));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void shouldPropagateInsufficientFundsException_WhenDomainLogicThrowsIt() {
        // Arrange
        String targetCbuStr = "1234567890123456789012";
        Cbu targetCbu = Cbu.of(targetCbuStr);
        BigDecimal amount = new BigDecimal("600.00");
        String idempotencyKey = UUID.randomUUID().toString();
        CreateTransferInputRequest request = new CreateTransferInputRequest(ORIGIN_ACCOUNT_ID, targetCbuStr, amount, "Pago", idempotencyKey);

        Account originAccount = spy(createTestAccount(ORIGIN_ACCOUNT_ID, "1111111111111111111111", "alias1", new BigDecimal("100")));

        when(transferRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(accountRepository.existsByCbu(targetCbu)).thenReturn(true);
        when(accountRepository.findById(ORIGIN_ACCOUNT_ID)).thenReturn(Optional.of(originAccount));
        doThrow(new InsufficientFundsException("Error", ORIGIN_ACCOUNT_ID, amount, originAccount.getBalance().value()))
                .when(originAccount).initiateTransferTo(any(), any(), any(), any());

        // Act & Assert
        assertThatThrownBy(() -> createTransferUseCase.createTransfer(request))
                .isInstanceOf(InsufficientFundsException.class);

        // Verify
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldPropagateSameAccountTransferException_WhenDomainLogicThrowsIt() {
        // Arrange
        String sameCbuStr = "1111111111111111111111";
        Cbu sameCbu = Cbu.of(sameCbuStr);
        String idempotencyKey = UUID.randomUUID().toString();
        CreateTransferInputRequest request = new CreateTransferInputRequest(ORIGIN_ACCOUNT_ID, sameCbuStr, new BigDecimal("100"), "Pago", idempotencyKey);

        Account originAccount = spy(createTestAccount(ORIGIN_ACCOUNT_ID, sameCbuStr, "alias1", new BigDecimal("500")));

        when(transferRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(accountRepository.existsByCbu(sameCbu)).thenReturn(true);
        when(accountRepository.findById(ORIGIN_ACCOUNT_ID)).thenReturn(Optional.of(originAccount));
        doThrow(new SameAccountTransferException("Error")).when(originAccount).initiateTransferTo(any(), any(), any(), any());

        // Act & Assert
        assertThatThrownBy(() -> createTransferUseCase.createTransfer(request))
                .isInstanceOf(SameAccountTransferException.class);

        // Verify
        verify(transferRepository, never()).save(any(Transfer.class));
    }
    
    @Test
    void shouldThrowAccountNotFoundException_WhenOriginAccountDoesNotExist() {
        // Arrange
        UUID nonExistentAccountId = UUID.randomUUID();
        String validCbu = "1234567890123456789012";
        // Corrected: Use a valid description
        CreateTransferInputRequest request = new CreateTransferInputRequest(nonExistentAccountId, validCbu, BigDecimal.ONE, "Test Description", "key");
        
        when(transferRepository.findByIdempotencyKey("key")).thenReturn(Optional.empty());
        when(accountRepository.existsByCbu(any(Cbu.class))).thenReturn(true); // Assume destination exists
        when(accountRepository.findById(nonExistentAccountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> createTransferUseCase.createTransfer(request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void shouldThrowInvalidAccountDataException_WhenTargetCbuDoesNotExist() {
        // Arrange
        String nonExistentCbuStr = "0000000000000000000000";
        Cbu nonExistentCbu = Cbu.of(nonExistentCbuStr);
        // Corrected: Use a valid description
        CreateTransferInputRequest request = new CreateTransferInputRequest(ORIGIN_ACCOUNT_ID, nonExistentCbuStr, BigDecimal.ONE, "Test Description", "key");
        
        when(transferRepository.findByIdempotencyKey("key")).thenReturn(Optional.empty());
        when(accountRepository.existsByCbu(nonExistentCbu)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> createTransferUseCase.createTransfer(request))
                .isInstanceOf(InvalidAccountDataException.class);
    }
    
    @Test
    void shouldThrowInvalidTransferDataException_WhenCbuFormatIsInvalid() {
        // Arrange
        String invalidCbu = "123";
        CreateTransferInputRequest request = new CreateTransferInputRequest(ORIGIN_ACCOUNT_ID, invalidCbu, BigDecimal.ONE, "", "key");

        // Act & Assert
        assertThatThrownBy(() -> createTransferUseCase.createTransfer(request))
                .isInstanceOf(InvalidTransferDataException.class);
    }
    
    private Account createTestAccount(UUID id, String cbu, String alias, BigDecimal balance) {
        return Account.withId(id, USER_ID, cbu, alias, balance, LocalDateTime.now());
    }

    private Transfer createTestTransfer(UUID id, CreateTransferInputRequest request) {
        return Transfer.reconstruct(
                id,
                IdempotencyKey.of(request.idempotencyKey()),
                request.originAccountId(),
                Cbu.of(request.targetCbu()),
                TransferAmount.of(request.amount()),
                TransferDescription.of(request.description()),
                com.homebanking.domain.enums.TransferStatus.PENDING,
                LocalDateTime.now(),
                null, null, null, 0, null
        );
    }
}
