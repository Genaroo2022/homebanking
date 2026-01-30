package com.homebanking.application.usecase.transfer;

import com.homebanking.application.dto.transfer.request.CreateTransferInputRequest;
import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.account.AccountNotFoundException;
import com.homebanking.domain.exception.transfer.DestinationAccountNotFoundException;
import com.homebanking.domain.exception.transfer.InsufficientFundsException;
import com.homebanking.domain.exception.transfer.SameAccountTransferException;
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

@RequiredArgsConstructor
@Slf4j
public class CreateTransferUseCaseImpl implements CreateTransferInputPort {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public TransferOutputResponse createTransfer(CreateTransferInputRequest request) {
        Optional<Transfer> existing = transferRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            return toOutputResponse(existing.get());
        }

        Account originAccount = loadOriginAccount(request.originAccountId());

        validateDestinationAccountExists(request.targetCbu());
        validateNotSameAccount(originAccount, request.targetCbu());
        validateSufficientFunds(originAccount, request.amount());

        Transfer transfer = buildTransfer(request);
        originAccount.debit(request.amount());

        Transfer savedTransfer = persistTransferAndAccount(transfer, originAccount);
        logTransferCreated(savedTransfer);

        eventPublisher.publish(new TransferCreatedEvent(savedTransfer.getId()));

        return toOutputResponse(savedTransfer);
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

    private Account loadOriginAccount(Long originAccountId) {
        return accountRepository.findById(originAccountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        DomainErrorMessages.ACCOUNT_NOT_FOUND,
                        originAccountId
                ));
    }

    private void validateDestinationAccountExists(String targetCbu) {
        if (accountRepository.findByCbu(targetCbu).isEmpty()) {
            throw new DestinationAccountNotFoundException(
                    DomainErrorMessages.ACCOUNT_NOT_FOUND,
                    targetCbu
            );
        }
    }

    private void validateNotSameAccount(Account originAccount, String targetCbu) {
        if (originAccount.getCbu().value().equals(targetCbu)) {
            throw new SameAccountTransferException(DomainErrorMessages.TRANSFER_SAME_ACCOUNT);
        }
    }

    private void validateSufficientFunds(Account originAccount, java.math.BigDecimal amount) {
        if (originAccount.getBalance().value().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    DomainErrorMessages.INSUFFICIENT_FUNDS,
                    originAccount.getId(),
                    amount,
                    originAccount.getBalance().value()
            );
        }
    }

    private Transfer buildTransfer(CreateTransferInputRequest request) {
        return Transfer.create(
                request.originAccountId(),
                Cbu.of(request.targetCbu()),
                TransferAmount.of(request.amount()),
                TransferDescription.of(request.description()),
                IdempotencyKey.of(request.idempotencyKey())
        );
    }

    private Transfer persistTransferAndAccount(Transfer transfer, Account originAccount) {
        Transfer savedTransfer = transferRepository.save(transfer);
        accountRepository.save(originAccount);
        return savedTransfer;
    }

    private void logTransferCreated(Transfer transfer) {
        log.info("Transferencia creada: id={}, idempotencyKey={}",
                transfer.getId(), transfer.getIdempotencyKey().value());
    }
}
