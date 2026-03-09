package com.homebanking.adapter.in.event;

import com.homebanking.domain.event.TransferCompletedEvent;
import com.homebanking.domain.event.TransferFailedEvent;
import com.homebanking.port.out.notification.NotificationOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationOutputPort notificationPort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransferCompleted(TransferCompletedEvent event) {
        log.info("Handling transfer completed event for transferId: {}", event.transferId());
        notificationPort.notifyTransferCompleted(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransferFailed(TransferFailedEvent event) {
        log.info("Handling transfer failed event for transferId: {}", event.transferId());
        notificationPort.notifyTransferFailed(event);
    }
}


