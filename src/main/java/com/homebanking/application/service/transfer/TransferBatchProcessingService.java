package com.homebanking.application.service.transfer;

import com.homebanking.domain.entity.Transfer;
import com.homebanking.port.in.transfer.ProcessTransferInputPort;
import com.homebanking.port.in.transfer.RetryTransferInputPort;
import com.homebanking.port.out.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service: TransferBatchProcessingService

 * Orquesta procesamiento en background (scheduler).
 */
@RequiredArgsConstructor
@Slf4j
public class TransferBatchProcessingService {

    private final TransferRepository transferRepository;
    private final ProcessTransferInputPort processTransferUseCase;
    private final RetryTransferInputPort retryFailedTransferUseCase;

    @Transactional
    public void processTransfers() {
        List<Transfer> pendingTransfers = transferRepository.findPendingTransfers();

        processBatch(
                pendingTransfers,
                "No hay transferencias pendientes para procesar",
                "Procesando {} transferencias pendientes",
                transfer -> processTransferUseCase.processTransfer(transfer.getId()),
                "Error procesando transferencia id={}: {}"
        );
    }

    @Transactional
    public void retryFailedTransfers() {
        List<Transfer> retryableTransfers = transferRepository.findRetryableTransfers();

        processBatch(
                retryableTransfers,
                "No hay transferencias para reintentar",
                "Reintentando {} transferencias fallidas",
                transfer -> retryFailedTransferUseCase.retryFailedTransfer(transfer.getId()),
                "Error reintentando transferencia id={}: {}"
        );
    }

    private void processBatch(
            List<Transfer> transfers,
            String emptyMessage,
            String startMessage,
            TransferBatchAction action,
            String errorMessage
    ) {
        if (transfers.isEmpty()) {
            log.debug(emptyMessage);
            return;
        }

        log.info(startMessage, transfers.size());

        transfers.forEach(transfer -> {
            try {
                action.execute(transfer);
            } catch (Exception ex) {
                log.error(errorMessage, transfer.getId(), ex.getMessage(), ex);
            }
        });
    }

    @FunctionalInterface
    private interface TransferBatchAction {
        void execute(Transfer transfer);
    }}

