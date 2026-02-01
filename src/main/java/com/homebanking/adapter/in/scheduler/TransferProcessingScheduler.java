package com.homebanking.adapter.in.scheduler;

import com.homebanking.application.service.transfer.TransferBatchProcessingService;
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

    private final TransferBatchProcessingService transferBatchProcessingService;

    @Scheduled(
            fixedDelayString = "${transfer.processor.fixed-delay:5000}",
            initialDelayString = "${transfer.processor.initial-delay:10000}"
    )
    public void processTransfers() {
        transferBatchProcessingService.processTransfers();
    }

    @Scheduled(
            fixedDelayString = "${transfer.processor.retry.fixed-delay:30000}",
            initialDelayString = "${transfer.processor.retry.initial-delay:30000}"
    )
    public void retryFailedTransfers() {
        transferBatchProcessingService.retryFailedTransfers();
    }
}



