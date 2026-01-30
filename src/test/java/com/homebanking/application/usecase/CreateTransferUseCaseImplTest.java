package com.homebanking.application.usecase;

import com.homebanking.application.dto.transfer.request.CreateTransferInputRequest;
import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.application.usecase.transfer.CreateTransferUseCaseImpl;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.account.AccountNotFoundException;
import com.homebanking.domain.exception.transfer.InsufficientFundsException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests: CreateTransferUseCaseImplTest

 * Verifica:
 * ✓ Idempotencia: misma key = mismo resultado sin duplicado
 * ✓ Validaciones de dominio: saldo, cuenta, CBU
 * ✓ Transaccionalidad: ambos cambios persisten juntos
 * ✓ Manejo de excepciones: códigos correctos

 * Patrón AAA: Arrange, Act, Assert
 * Patrón Given-When-Then
 */
@ExtendWith(MockitoExtension.class)
class CreateTransferUseCaseImplTest {

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

    // ===============================================
    // TESTS: CASOS DE ÉXITO
    // ===============================================

    /**
     * TEST: Crear transferencia exitosamente

     * GIVEN: Cuenta origen existe con saldo suficiente
     * WHEN: Se crea una transferencia
     * THEN: Se retorna respuesta con ID y estado PENDING
     */
    @Test
    void shouldCreateTransferSuccessfully_WhenAccountHasSufficientBalance() {
        // Arrange
        Long originAccountId = 1L;
        String targetCbu = "1234567890123456789012";
        BigDecimal amount = new BigDecimal("100.00");
        String idempotencyKey = UUID.randomUUID().toString();

        CreateTransferInputRequest request = new CreateTransferInputRequest(
                originAccountId,
                targetCbu,
                amount,
                "Pago de servicios",
                idempotencyKey
        );

        // Cuenta origen con saldo suficiente
        Account originAccount = createTestAccount(
                originAccountId,
                "1111111111111111111111",
                "john.doe.123",
                new BigDecimal("500.00")
        );
        Account destinationAccount = createTestAccount(
                2L,
                targetCbu,
                "target.user.456",
                new BigDecimal("0.00")
        );

        Transfer savedTransfer = createTestTransfer(1L, request);

        // Configurar mocks
        when(transferRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());
        when(accountRepository.findById(originAccountId))
                .thenReturn(Optional.of(originAccount));
        when(accountRepository.findByCbu(request.targetCbu()))
                .thenReturn(Optional.of(createTestAccount(
                        2L,
                        request.targetCbu(),
                        "dest.user.004",
                        BigDecimal.ZERO
                )));
        when(accountRepository.findByCbu(request.targetCbu()))
                .thenReturn(Optional.of(createTestAccount(
                        2L,
                        request.targetCbu(),
                        "dest.user.004",
                        BigDecimal.ZERO
                )));
        when(accountRepository.findByCbu(request.targetCbu()))
                .thenReturn(Optional.of(createTestAccount(
                        2L,
                        request.targetCbu(),
                        "dest.user.004",
                        BigDecimal.ZERO
                )));
        when(accountRepository.findByCbu(request.targetCbu()))
                .thenReturn(Optional.of(createTestAccount(
                        2L,
                        request.targetCbu(),
                        "dest.user.003",
                        BigDecimal.ZERO
                )));
        when(accountRepository.findByCbu(request.targetCbu()))
                .thenReturn(Optional.of(createTestAccount(
                        2L,
                        request.targetCbu(),
                        "dest.user.001",
                        BigDecimal.ZERO
                )));
        when(accountRepository.findByCbu(targetCbu))
                .thenReturn(Optional.of(destinationAccount));
        when(transferRepository.save(any(Transfer.class)))
                .thenReturn(savedTransfer);
        when(accountRepository.save(any(Account.class)))
                .thenReturn(originAccount);

        // Act
        TransferOutputResponse result = createTransferUseCase.createTransfer(request);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(transfer -> {
                    assertThat(transfer.id()).isEqualTo(1L);
                    assertThat(transfer.status()).isEqualTo("PENDING");
                    assertThat(transfer.amount()).isEqualTo(amount);
                });

        // Verify: Se debitó la cuenta y se guardó la transferencia
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        Account savedAccount = accountCaptor.getValue();
        assertThat(savedAccount.getBalance().value()).isEqualTo(new BigDecimal("400.00"));

        verify(transferRepository).save(any(Transfer.class));
        verify(eventPublisher).publish(any(com.homebanking.domain.event.TransferCreatedEvent.class));
    }

    /**
     * TEST: Idempotencia - transferencia duplicada retorna la primera

     * GIVEN: Existe una transferencia con el mismo idempotencyKey
     * WHEN: Se intenta crear otra con el mismo key
     * THEN: Retorna la transferencia existente sin crear duplicado
     */
    @Test
    void shouldReturnExistingTransfer_WhenIdempotencyKeyAlreadyExists() {
        // Arrange
        String idempotencyKey = UUID.randomUUID().toString();

        CreateTransferInputRequest request = new CreateTransferInputRequest(
                1L,
                "1234567890123456789012",
                new BigDecimal("100.00"),
                "Pago",
                idempotencyKey
        );

        Transfer existingTransfer = createTestTransfer(1L, request);

        // Configurar mock: la transferencia ya existe
        when(transferRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.of(existingTransfer));

        // Act
        TransferOutputResponse result = createTransferUseCase.createTransfer(request);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(transfer -> {
                    assertThat(transfer.idempotencyKey()).isEqualTo(idempotencyKey);
                    assertThat(transfer.id()).isEqualTo(1L);
                });

        // Verify: NO se creó nueva transferencia
        verify(accountRepository, never()).findById(anyLong());
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    // ===============================================
    // TESTS: CASOS DE ERROR
    // ===============================================

    /**
     * TEST: Rechaza transferencia si saldo insuficiente

     * GIVEN: Cuenta origen existe pero saldo < monto a transferir
     * WHEN: Se intenta crear transferencia
     * THEN: Lanza InsufficientFundsException
     */
    @Test
    void shouldThrowInsufficientFundsException_WhenAccountHasLowBalance() {
        // Arrange
        Long originAccountId = 1L;
        BigDecimal amount = new BigDecimal("600.00");
        String idempotencyKey = UUID.randomUUID().toString();

        CreateTransferInputRequest request = new CreateTransferInputRequest(
                originAccountId,
                "1234567890123456789012",
                amount,
                "Pago",
                idempotencyKey
        );

        // Cuenta con poco saldo
        Account originAccount = createTestAccount(
                originAccountId,
                "1111111111111111111111",
                "john.doe.123",
                new BigDecimal("100.00")
        );

        when(transferRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());
        when(accountRepository.findById(originAccountId))
                .thenReturn(Optional.of(originAccount));
        when(accountRepository.findByCbu(request.targetCbu()))
                .thenReturn(Optional.of(createTestAccount(
                        2L,
                        request.targetCbu(),
                        "dest.user.004",
                        BigDecimal.ZERO
                )));

        // Act & Assert
        assertThatThrownBy(() -> createTransferUseCase.createTransfer(request))
                .isInstanceOf(InsufficientFundsException.class)
                .satisfies(ex -> {
                    InsufficientFundsException ifEx = (InsufficientFundsException) ex;
                    assertThat(ifEx.getAccountId()).isEqualTo(originAccountId);
                    assertThat(ifEx.getRequestedAmount()).isEqualTo(amount);
                    assertThat(ifEx.getAvailableBalance()).isEqualTo(new BigDecimal("100.00"));
                });

        // Verify: No se persistió nada
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    /**
     * TEST: Rechaza transferencia a la misma cuenta

     * GIVEN: CBU origen == CBU destino
     * WHEN: Se intenta crear transferencia
     * THEN: Lanza SameAccountTransferException
     */
    @Test
    void shouldThrowSameAccountTransferException_WhenOriginAndTargetAreSame() {
        // Arrange
        String sameCbu = "1111111111111111111111";
        String idempotencyKey = UUID.randomUUID().toString();

        CreateTransferInputRequest request = new CreateTransferInputRequest(
                1L,
                sameCbu,  // CBU igual al origen
                new BigDecimal("100.00"),
                "Pago",
                idempotencyKey
        );

        Account originAccount = createTestAccount(
                1L,
                sameCbu,  // Mismo CBU
                "john.doe.123",
                new BigDecimal("500.00")
        );

        when(transferRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());
        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(originAccount));
        when(accountRepository.findByCbu(sameCbu))
                .thenReturn(Optional.of(originAccount));

        // Act & Assert
        assertThatThrownBy(() -> createTransferUseCase.createTransfer(request))
                .isInstanceOf(SameAccountTransferException.class);

        verify(transferRepository, never()).save(any(Transfer.class));
    }

    /**
     * TEST: Rechaza si cuenta origen no existe

     * GIVEN: No existe cuenta con ese ID
     * WHEN: Se intenta crear transferencia
     * THEN: Lanza AccountNotFoundException
     */
    @Test
    void shouldThrowAccountNotFoundException_WhenOriginAccountDoesNotExist() {
        // Arrange
        Long nonExistentAccountId = 999L;
        String idempotencyKey = UUID.randomUUID().toString();

        CreateTransferInputRequest request = new CreateTransferInputRequest(
                nonExistentAccountId,
                "1234567890123456789012",
                new BigDecimal("100.00"),
                "Pago",
                idempotencyKey
        );

        when(transferRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());
        when(accountRepository.findById(nonExistentAccountId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> createTransferUseCase.createTransfer(request))
                .isInstanceOf(AccountNotFoundException.class)
                .satisfies(ex -> {
                    AccountNotFoundException anfEx = (AccountNotFoundException) ex;
                    assertThat(anfEx.getAccountId()).isEqualTo(nonExistentAccountId);
                });

        verify(transferRepository, never()).save(any(Transfer.class));
    }

    /**
     * TEST: Rechaza CBU inválido

     * GIVEN: CBU no tiene 22 dígitos
     * WHEN: Se intenta crear transferencia
     * THEN: Lanza InvalidTransferDataException
     */
    @Test
    void shouldThrowInvalidTransferDataException_WhenCbuFormatIsInvalid() {
        // Arrange
        String invalidCbu = "123";  // CBU muy corto
        String idempotencyKey = UUID.randomUUID().toString();

        CreateTransferInputRequest request = new CreateTransferInputRequest(
                1L,
                invalidCbu,
                new BigDecimal("100.00"),
                "Pago",
                idempotencyKey
        );

        Account originAccount = createTestAccount(1L, "1111111111111111111111",
                "john.doe.123", new BigDecimal("500.00"));

        when(transferRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());
        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(originAccount));
        when(accountRepository.findByCbu(invalidCbu))
                .thenReturn(Optional.of(createTestAccount(
                        2L,
                        "1234567890123456789012",
                        "dest.user.002",
                        BigDecimal.ZERO
                )));

        // Act & Assert
        assertThatThrownBy(() -> createTransferUseCase.createTransfer(request))
                .isInstanceOf(InvalidTransferDataException.class);

        verify(transferRepository, never()).save(any(Transfer.class));
    }

    // ===============================================
    // MÉTODOS HELPER
    // ===============================================

    private Account createTestAccount(Long id, String cbu, String alias, BigDecimal balance) {
        return Account.withId(
                id,
                1L,  // userId
                cbu,
                alias,
                balance,
                LocalDateTime.now()
        );
    }

    private Transfer createTestTransfer(Long id, CreateTransferInputRequest request) {
        return Transfer.reconstruct(
                id,
                IdempotencyKey.of(request.idempotencyKey()),
                request.originAccountId(),
                Cbu.of(request.targetCbu()),
                TransferAmount.of(request.amount()),
                TransferDescription.of(request.description()),
                com.homebanking.domain.enums.TransferStatus.PENDING,
                LocalDateTime.now(),
                null,
                null,
                null,
                0,
                null
        );
    }
}
