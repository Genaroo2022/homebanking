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
 * Service: ProcessTransferApplicationService
 *
 * Orquesta procesamiento en background (scheduler).
 */
@RequiredArgsConstructor
@Slf4j
public class ProcessTransferApplicationService {

    private final TransferRepository transferRepository;
    private final ProcessTransferInputPort processTransferUseCase;
    private final RetryTransferInputPort retryFailedTransferUseCase;

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
                processTransferUseCase.processTransfer(transfer.getId());
            } catch (Exception ex) {
                log.error("Error procesando transferencia id={}: {}",
                        transfer.getId(), ex.getMessage(), ex);
            }
        });
    }

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
                retryFailedTransferUseCase.retryFailedTransfer(transfer.getId());
            } catch (Exception ex) {
                log.error("Error reintentando transferencia id={}: {}",
                        transfer.getId(), ex.getMessage(), ex);
            }
        });
    }
}
