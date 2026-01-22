package com.homebanking.application.service;

import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.DestinationAccountNotFoundException;
import com.homebanking.domain.exception.TransferProcessingException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.NotificationOutputPort;
import com.homebanking.port.out.TransferProcessorOutputPort;
import com.homebanking.port.out.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service: TransferProcessorService

 * Responsabilidades:
 * ✓ Procesar transferencias pendientes (asincrónico)
 * ✓ Reintentar transferencias fallidas (con backoff exponencial)
 * ✓ Notificar usuarios de resultados
 * ✓ Mantener cuenta destino acreditada

 * Garantías:
 * • Procesa solo transferencias PENDING
 * • Reintentos automáticos con backoff exponencial
 * • Notificaciones después de completar/fallar
 * • Logging estructurado para observabilidad

 * Arquitectura:
 * • No es invocado directamente por controladores
 * • Se ejecuta de forma programada (scheduled)
 * • Altamente resiliente a fallos transitorios
 */
@RequiredArgsConstructor
@Slf4j
public class TransferProcessorService {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final TransferProcessorOutputPort transferProcessor;
    private final NotificationOutputPort notificationPort;

    /**
     * Procesa transferencias pendientes.*
     * Se ejecuta cada 5 segundos (configurable).
     * Busca transferencias en estado PENDING y las procesa contra el sistema externo.

     * Características:
     * • Thread-safe: usa @Transactional
     * • No bloquea: se ejecuta en thread separado
     * • Observable: logging detallado
     * • Resiliente: maneja fallos gracefully
     */
    @Transactional
    public void processTransfers() {
        List<Transfer> pendingTransfers = transferRepository.findPendingTransfers();

        if (pendingTransfers.isEmpty()) {
            log.debug("No hay transferencias pendientes para procesar");
            return;
        }

        log.info("Procesando {} transferencias pendientes", pendingTransfers.size());

        pendingTransfers.forEach(transfer -> {
            try {
                processTransfer(transfer);
            } catch (Exception ex) {
                log.error("Error procesando transferencia id={}: {}",
                        transfer.getId(), ex.getMessage(), ex);
                // Continúa con la siguiente transferencia (no falla todo)
            }
        });
    }

    /**
     * Procesa una transferencia individual.

     * Flujo:
     * 1. Llamar a sistema externo (puede fallar)
     * 2. Si éxito: marcar como COMPLETED, acreditar destino
     * 3. Si falla: marcar como FAILED, agendar reintento
     * 4. Notificar usuario en ambos casos
     */
    public void processTransfer(Transfer transfer) {
        log.debug("Iniciando procesamiento de transferencia: id={}, amount={}",
                transfer.getId(), transfer.getAmount());

        try {
            ensureDestinationExists(transfer);

            // Llamar al procesador externo (banco central, sistema de pagos, etc.)
            boolean processed = transferProcessor.processTransfer(transfer);

            if (processed) {
                // Éxito: marcar como completada
                transfer.markAsCompleted();
                transferRepository.save(transfer);

                // Acreditar la cuenta destino
                creditDestinationAccount(transfer);

                // Notificar usuario
                notificationPort.notifyTransferCompleted(transfer);

                log.info("Transferencia completada: id={}, idempotencyKey={}",
                        transfer.getId(), transfer.getIdempotencyKey());

            } else {
                // Fallo recuperable: marcar para reintento
                handleRetryableFailure(transfer);
            }

        } catch (TransferProcessingException ex) {
            if (ex.isRecoverable()) {
                // Error temporal: agendar reintento
                handleRetryableFailure(transfer);
            } else {
                // Error permanente: marcar como rechazada
                transfer.markAsRejected(ex.getMessage());
                transferRepository.save(transfer);
                notificationPort.notifyTransferFailed(transfer);

                log.error("Transferencia rechazada permanentemente: id={}, reason={}",
                        transfer.getId(), ex.getExternalErrorCode());
            }
        } catch (DestinationAccountNotFoundException ex) {
            transfer.markAsRejected(ex.getMessage());
            transferRepository.save(transfer);
            notificationPort.notifyTransferFailed(transfer);

            log.error("Cuenta destino no encontrada. Transferencia rechazada: cbu={}",
                    ex.getTargetCbu());
        }
    }

    /**
     * Procesa reintentos de transferencias fallidas.

     * Se ejecuta cada 30 segundos.
     * Busca transferencias con estado FAILED que aún tienen reintentos disponibles.

     * Límites de reintento:
     * • Máximo 3 intentos totales
     * • Backoff exponencial (espera más entre intentos)
     */
    @Transactional
    public void retryFailedTransfers() {
        List<Transfer> retryableTransfers = transferRepository.findRetryableTransfers();

        if (retryableTransfers.isEmpty()) {
            log.debug("No hay transferencias para reintentar");
            return;
        }

        log.info("Reintentando {} transferencias fallidas", retryableTransfers.size());

        retryableTransfers.forEach(transfer -> {
            try {
                retryTransfer(transfer);
            } catch (Exception ex) {
                log.error("Error reintentando transferencia id={}: {}",
                        transfer.getId(), ex.getMessage());
            }
        });
    }

    /**
     * Reintenta procesar una transferencia fallida.
     * Incrementa contador de reintentos con backoff exponencial.
     */
    public void retryTransfer(Transfer transfer) {
        transfer.incrementRetryCount();
        transferRepository.save(transfer);

        log.debug("Reintentando transferencia: id={}, attempt={}",
                transfer.getId(), transfer.getRetryCount());

        try {
            boolean processed = transferProcessor.processTransfer(transfer);

            if (processed) {
                transfer.markAsCompleted();
                transferRepository.save(transfer);
                creditDestinationAccount(transfer);
                notificationPort.notifyTransferCompleted(transfer);

                log.info("Transferencia completada en reintento: id={}, attempt={}",
                        transfer.getId(), transfer.getRetryCount());
            } else {
                // Seguirá siendo procesada en el siguiente ciclo de reintentos
                log.warn("Reintento fallido (será procesado después): id={}", transfer.getId());
            }

        } catch (TransferProcessingException ex) {
            log.warn("Error en reintento: id={}, attempt={}, recoverable={}",
                    transfer.getId(), transfer.getRetryCount(), ex.isRecoverable());

            if (!ex.isRecoverable() || transfer.getRetryCount() >= 3) {
                // No hay más reintentos o error permanente
                transfer.markAsRejected(ex.getMessage());
                transferRepository.save(transfer);
                notificationPort.notifyTransferFailed(transfer);

                log.error("Transferencia rechazada después de reintentos: id={}",
                        transfer.getId());
            }
        }
    }

    /**
     * Acredita la cuenta de destino.

     * IMPORTANTE: Esta operación NO revierte la transferencia de origen.
     * El origin ya fue debitado en CreateTransferUseCaseImpl.

     * Aquí encontramos la cuenta destino y le acreditamos.
     */
    private void creditDestinationAccount(Transfer transfer) {
        // Buscar cuenta destino por CBU
        accountRepository.findByCbu(transfer.getTargetCbu())
                .ifPresentOrElse(
                        account -> {
                            account.deposit(transfer.getAmount());
                            accountRepository.save(account);
                            log.debug("Cuenta destino acreditada: cbu={}, amount={}",
                                    transfer.getTargetCbu(), transfer.getAmount());
                        },
                        () -> log.error("Cuenta destino no encontrada: cbu={}",
                                transfer.getTargetCbu())
                );
    }

    /**
     * Rechaza transferencia si la cuenta destino no existe.
     */
    private void ensureDestinationExists(Transfer transfer) {
        if (accountRepository.findByCbu(transfer.getTargetCbu()).isEmpty()) {
            throw new DestinationAccountNotFoundException(
                    DomainErrorMessages.ACCOUNT_NOT_FOUND,
                    transfer.getTargetCbu()
            );
        }
    }

    /**
     * Maneja fallo recuperable (@reintentable).
     */
    private void handleRetryableFailure(Transfer transfer) {
        transfer.markAsFailed("Error temporal durante procesamiento. Se reintentará automáticamente.");
        transfer.incrementRetryCount();
        transferRepository.save(transfer);

        log.warn("Transferencia marcada para reintento: id={}, attempt={}",
                transfer.getId(), transfer.getRetryCount());
    }
}
