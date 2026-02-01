package com.homebanking.application.usecase.transfer;

import com.homebanking.application.dto.transfer.request.CreateTransferInputRequest;
import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.account.AccountNotFoundException;
import com.homebanking.domain.exception.account.InvalidAccountDataException;
import com.homebanking.domain.exception.transfer.DestinationAccountNotFoundException;
import com.homebanking.domain.event.TransferCreatedEvent;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.domain.valueobject.common.Cbu;
import com.homebanking.domain.valueobject.transfer.IdempotencyKey;
import com.homebanking.domain.valueobject.transfer.TransferAmount;
import com.homebanking.domain.valueobject.transfer.TransferDescription;
import com.homebanking.port.in.transfer.CreateTransferInputPort;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.EventPublisher;
import com.homebanking.port.out.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class CreateTransferUseCaseImpl implements CreateTransferInputPort {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public TransferOutputResponse createTransfer(CreateTransferInputRequest request) {
        // 1. Idempotencia
        Optional<Transfer> existing = transferRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            return toOutputResponse(existing.get());
        }

        // 2. Conversión de Value Objects (Fail-Fast)
        Cbu targetCbu = Cbu.of(request.targetCbu());
        TransferAmount amount = TransferAmount.of(request.amount());
        TransferDescription description = TransferDescription.of(request.description());
        IdempotencyKey key = IdempotencyKey.of(request.idempotencyKey());

        // 3. Validar existencia del destino (Opcional pero recomendado)
        // NOTA: No cargamos la entidad, solo verificamos si es viable transferirle.
        // Si el destino es otro banco, esta validación podría no aplicar o ser diferente.
        validateDestinationExists(targetCbu);

        // 4. Cargar Agregado Origen
        Account originAccount = loadOriginAccount(request.originAccountId());

        // 5. Lógica de Negocio (Dominio Puro)
        // Aquí ocurre la magia: Pasamos solo el CBU destino.
        Transfer transfer = originAccount.initiateTransferTo(
                targetCbu,
                amount,
                description,
                key
        );

        // 6. Persistencia (Transaccionalidad atómica del agregado origen y la transferencia)
        persistTransferAndAccount(transfer, originAccount);

        // 7. Efectos secundarios (Logs y Eventos)
        logTransferCreated(transfer);
        eventPublisher.publish(new TransferCreatedEvent(transfer.getId()));

        return toOutputResponse(transfer);
    }

    private void validateDestinationExists(Cbu targetCbu) {
        // Esto es mucho más ligero que un findById().
        // Solo verifica "SELECT 1 FROM accounts WHERE cbu = ?"
        if (!accountRepository.existsByCbu(targetCbu)) {
            throw new InvalidAccountDataException(DomainErrorMessages.ACCOUNT_NOT_FOUND);
        }
    }

    private TransferOutputResponse toOutputResponse(Transfer transfer) {
        return new TransferOutputResponse(
                transfer.getId(),
                transfer.getIdempotencyKey().value(),
                transfer.getOriginAccountId(),
                transfer.getTargetCbu().value(),
                transfer.getAmount().value(),
                transfer.getDescription().value(),
                transfer.getStatus().name(),
                transfer.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    private Account loadOriginAccount(UUID originAccountId) {
        return accountRepository.findById(originAccountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        DomainErrorMessages.ACCOUNT_NOT_FOUND,
                        originAccountId
                ));
    }

    private void persistTransferAndAccount(Transfer transfer, Account originAccount) {
        transferRepository.save(transfer);
        accountRepository.save(originAccount);
    }

    private void logTransferCreated(Transfer transfer) {
        log.info("Transferencia creada: id={}, idempotencyKey={}",
                transfer.getId(), transfer.getIdempotencyKey().value());
    }
}
