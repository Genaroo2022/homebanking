package com.homebanking.adapter.in.scheduler;

import com.homebanking.application.service.transfer.ProcessTransferApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Adapter in: Scheduler for transfer processing.

 * Mantiene el scheduling fuera de la capa de aplicacion.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransferProcessingScheduler {

    private final ProcessTransferApplicationService processTransferApplicationService;

    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void processTransfers() {
        processTransferApplicationService.processTransfers();
    }

    @Scheduled(fixedDelay = 30000, initialDelay = 30000)
    public void retryFailedTransfers() {
        processTransferApplicationService.retryFailedTransfers();
    }
}
