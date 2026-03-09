package com.homebanking.adapter.in.scheduler;

import com.homebanking.application.service.transfer.TransferBatchProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Adapter in: Scheduler for transfer processing.

 * Mantiene el scheduling fuera de la capa de aplicacion.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransferProcessingScheduler {

    private final TransferBatchProcessingService transferBatchProcessingService;
    private final AtomicBoolean processingRunning = new AtomicBoolean(false);
    private final AtomicBoolean retryRunning = new AtomicBoolean(false);

    @Value("${transfer.processor.scheduler-enabled:true}")
    private boolean schedulerEnabled;

    @Scheduled(
            fixedDelayString = "${transfer.processor.fixed-delay:5000}",
            initialDelayString = "${transfer.processor.initial-delay:10000}"
    )
    public void processTransfers() {
        if (!schedulerEnabled || !processingRunning.compareAndSet(false, true)) {
            return;
        }
        try {
            transferBatchProcessingService.processTransfers();
        } finally {
            processingRunning.set(false);
        }
    }

    @Scheduled(
            fixedDelayString = "${transfer.processor.retry.fixed-delay:30000}",
            initialDelayString = "${transfer.processor.retry.initial-delay:30000}"
    )
    public void retryFailedTransfers() {
        if (!schedulerEnabled || !retryRunning.compareAndSet(false, true)) {
            return;
        }
        try {
            transferBatchProcessingService.retryFailedTransfers();
        } finally {
            retryRunning.set(false);
        }
    }
}



