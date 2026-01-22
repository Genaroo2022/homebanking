package com.homebanking.application.usecase;

import com.homebanking.application.dto.transfer.request.CreateTransferInputRequest;
import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.*;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.transfer.CreateTransferInputPort;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * Use Case: CreateTransferUseCaseImpl

 * Responsabilidades:
 * ✓ Validar que no exista ya una transferencia con mismo idempotencyKey
 * ✓ Validar que la cuenta origen existe y tiene saldo suficiente
 * ✓ Validar que el destino es diferente del origen
 * ✓ Crear entity Transfer (dominio)
 * ✓ Debitar de la cuenta origen (y proteger invariantes)
 * ✓ Persistir ambos cambios atómicamente
 * ✓ Logging estructurado para observabilidad

 * No responsable de:
 * ✗ Procesar transferencia (es asincrónico, otro servicio)
 * ✗ Notificar usuarios (otro adapter)
 * ✗ Persistencia (transferida a repository)

 * Garantías:
 * • IDEMPOTENTE: múltiples POST con mismo idempotencyKey => mismo resultado
 * • ATÓMICO: Si falla por cualquier motivo, nada se persiste
 * • TRANSACCIONAL: Cambios en Account + Transfer van juntos
 */
@RequiredArgsConstructor
@Slf4j
public class CreateTransferUseCaseImpl implements CreateTransferInputPort {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Crea una nueva transferencia de forma idempotente y atómica.

     * Flujo:
     * 1. Verificar idempotencia (¿existe con ese key?)
     * 2. Validar cuentas (origen existe, tiene saldo)
     * 3. Crear entidad Transfer
     * 4. Debitar la cuenta origen
     * 5. Persistir ambas cambios en transacción
     * 6. Retornar DTO con info de la transferencia creada

     * Garantías ACID:
     * @ Transactional asegura que si cualquier paso falla,
     * toda la transacción se revierte.
     */
    @Override
    @Transactional
    public TransferOutputResponse createTransfer(CreateTransferInputRequest request) {

        // PASO 1: Verificar idempotencia
        // Si ya existe una transferencia con ese key, devolverla sin duplicar
        var existingTransfer = transferRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existingTransfer.isPresent()) {
            log.info("Transferencia idempotente encontrada: key={}", request.idempotencyKey());
            return mapToResponse(existingTransfer.get());
        }

        log.info("Iniciando creación de transferencia: origin={}, target={}, amount={}, key={}",
                request.originAccountId(), request.targetCbu(), request.amount(),
                request.idempotencyKey());

        // PASO 2: Validar cuentas y saldo
        Account originAccount = accountRepository.findById(request.originAccountId())
                .orElseThrow(() -> {
                    log.warn("Cuenta origen no encontrada: {}", request.originAccountId());
                    return new AccountNotFoundException(
                            DomainErrorMessages.ACCOUNT_NOT_FOUND,
                            request.originAccountId()
                    );
                });

        // Validar que no sea transferencia a sí mismo
        if (originAccount.getCbu().equals(request.targetCbu())) {
            log.warn("Intento de transferencia a la misma cuenta: {}", request.originAccountId());
            throw new SameAccountTransferException(
                    DomainErrorMessages.TRANSFER_SAME_ACCOUNT
            );
        }

        // Validar saldo suficiente
        if (originAccount.getBalance().compareTo(request.amount()) < 0) {
            log.warn("Saldo insuficiente: account={}, requested={}, available={}",
                    request.originAccountId(), request.amount(), originAccount.getBalance());
            throw new InsufficientFundsException(
                    DomainErrorMessages.INSUFFICIENT_FUNDS,
                    request.originAccountId(),
                    request.amount(),
                    originAccount.getBalance()
            );
        }

        // PASO 3: Crear entidad de transferencia
        Transfer newTransfer = Transfer.createWithIdempotencyKey(
                request.originAccountId(),
                request.targetCbu(),
                request.amount(),
                request.description(),
                request.idempotencyKey()
        );

        // Validar que la cuenta destino exista
        accountRepository.findByCbu(request.targetCbu())
                .orElseThrow(() -> {
                    log.warn("Cuenta destino no encontrada: cbu={}", request.targetCbu());
                    return new DestinationAccountNotFoundException(
                            DomainErrorMessages.ACCOUNT_NOT_FOUND,
                            request.targetCbu()
                    );
                });

        // PASO 4: Debitar la cuenta origen
        // Esto valida que no quede con saldo negativo (protege invariante de dominio)
        originAccount.debit(request.amount());

        log.debug("Debitada cuenta origen: account={}, amount={}, newBalance={}",
                request.originAccountId(), request.amount(), originAccount.getBalance());

        // PASO 5: Persistir ambos cambios atómicamente
        // Si alguno falla, la transacción completa se revierte
        Transfer savedTransfer = transferRepository.save(newTransfer);
        accountRepository.save(originAccount);

        log.info("Transferencia creada exitosamente: id={}, key={}, status=PENDING",
                savedTransfer.getId(), savedTransfer.getIdempotencyKey());

        // PASO 6: Retornar respuesta
        return mapToResponse(savedTransfer);
    }

    /**
     * Mapea entidad Transfer a DTO de respuesta.
     */
    private TransferOutputResponse mapToResponse(Transfer transfer) {
        return new TransferOutputResponse(
                transfer.getId(),
                transfer.getIdempotencyKey(),
                transfer.getOriginAccountId(),
                transfer.getTargetCbu(),
                transfer.getAmount(),
                transfer.getDescription(),
                transfer.getStatus().name(),
                transfer.getCreatedAt().format(DATETIME_FORMATTER)
        );
    }
}
