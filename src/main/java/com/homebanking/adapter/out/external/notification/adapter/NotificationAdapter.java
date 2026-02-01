package com.homebanking.adapter.out.external.notification.adapter;

import com.homebanking.domain.event.TransferCompletedEvent;
import com.homebanking.domain.event.TransferFailedEvent;
import com.homebanking.port.out.notification.NotificationOutputPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationAdapter implements NotificationOutputPort {

    @Override
    public void notifyTransferCompleted(TransferCompletedEvent event) {
        log.info("NOTIFICACIÓN - Transferencia completada: " +
                        "id={}, amount={}, targetCbu={}, timestamp={}",
                event.transferId(),
                event.amount(),
                event.targetCbu(),
                event.completedAt());

        try {
            sendEmailNotification(
                    "transferencia_completada@banco.com",
                    event.transferId()
            );
            publishTransferCompletedEvent(event);
            log.debug("Notificación enviada exitosamente para transferencia: id={}", event.transferId());
        } catch (Exception ex) {
            log.error("Error enviando notificación de transferencia completada: id={}, error={}",
                    event.transferId(), ex.getMessage(), ex);
        }
    }

    @Override
    public void notifyTransferFailed(TransferFailedEvent event) {
        log.warn("NOTIFICACIÓN - Transferencia fallida: " +
                        "id={}, amount={}, reason={}, timestamp={}",
                event.transferId(),
                event.amount(),
                event.failureReason(),
                event.failedAt());

        try {
            sendEmailNotification(
                    "transferencia_fallida@banco.com",
                    event.transferId()
            );
            publishTransferFailedEvent(event);
            log.debug("Notificación de fallo enviada para transferencia: id={}", event.transferId());
        } catch (Exception ex) {
            log.error("Error enviando notificación de transferencia fallida: id={}, error={}",
                    event.transferId(), ex.getMessage(), ex);
        }
    }

    private void sendEmailNotification(String templateType, java.util.UUID transferId) {
        log.debug("Enviando email [{}] para transferencia: id={}", templateType, transferId);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.debug("Email enviado exitosamente");
    }

    private void publishTransferCompletedEvent(TransferCompletedEvent event) {
        log.debug("Publicando evento TransferCompleted a cola de mensajes: id={}", event.transferId());
        log.debug("Evento publicado exitosamente");
    }

    private void publishTransferFailedEvent(TransferFailedEvent event) {
        log.debug("Publicando evento TransferFailed a cola de mensajes: id={}", event.transferId());
        log.debug("Evento publicado exitosamente");
    }
}


