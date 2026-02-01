package com.homebanking.adapter.in.event;

import com.homebanking.domain.event.TransferCompletedEvent;
import com.homebanking.domain.event.TransferFailedEvent;
import com.homebanking.port.out.notification.NotificationOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationOutputPort notificationPort;

    @TransactionalEventListener
    public void handleTransferCompleted(TransferCompletedEvent event) {
        log.info("Handling transfer completed event for transferId: {}", event.transferId());
        // In a real application, we might re-fetch the entity or build a dedicated notification DTO
        // For now, we assume the NotificationOutputPort can handle this event data.
        // This decouples the notification from the use case.
        notificationPort.notifyTransferCompleted(event);
    }

    @TransactionalEventListener
    public void handleTransferFailed(TransferFailedEvent event) {
        log.info("Handling transfer failed event for transferId: {}", event.transferId());
        // Similar to the completed handler, the port should be adapted to this data
        notificationPort.notifyTransferFailed(event);
    }
}


