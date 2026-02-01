package com.homebanking.port.out;

import com.homebanking.domain.event.TransferCompletedEvent;
import com.homebanking.domain.event.TransferFailedEvent;

public interface NotificationOutputPort {

    /**
     * Notifica que una transferencia fue completada.
     * Puede ser email, SMS, push, etc.
     */
    void notifyTransferCompleted(TransferCompletedEvent event);

    /**
     * Notifica que una transferencia falló.
     */
    void notifyTransferFailed(TransferFailedEvent event);
}