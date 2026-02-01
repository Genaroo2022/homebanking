
package com.homebanking.adapter.out.external.notification.client;

import com.homebanking.domain.event.TransferCompletedEvent;
import com.homebanking.domain.event.TransferFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Event Listeners: Async event handlers

 * Cuando TransferCompletedEvent es publicado:
 * 1. Spring publica el evento
 * 2. Listeners son ejecutados ASINCRÓNICAMENTE
 * 3. Notificaciones enviadas sin bloquear transaction

 * Beneficios:
 * - UseCase no espera a que email sea enviado
 * - Si email falla, transfer ya fue guardado
 * - Escalable: Fácil agregar más listeners
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferNotificationListener {

    // Inyectar servicios de notificación
    // private final EmailService emailService;
    // private final SmsService smsService;

    @EventListener
    @Async  // Ejecutar en thread pool, no bloquear
    public void onTransferCompleted(TransferCompletedEvent event) {
        log.info("Sending transfer completion notification",
                "transferId", event.transferId(),
                "amount", event.amount());

        try {
            // emailService.sendTransferCompletionEmail(event);
            // smsService.sendTransferCompletionSms(event);

            log.info("Transfer notification sent successfully",
                    "transferId", event.transferId());

        } catch (Exception e) {
            // Importante: No propagar la excepción
            // El transfer ya fue completado, email es "best effort"
            log.error("Failed to send transfer notification",
                    "transferId", event.transferId(),
                    "error", e.getMessage());
        }
    }

    @EventListener
    @Async
    public void onTransferFailed(TransferFailedEvent event) {
        log.warn("Sending transfer failure notification",
                "transferId", event.transferId(),
                "reason", event.failureReason());

        try {
            // emailService.sendTransferFailureEmail(event);
            log.info("Transfer failure notification sent",
                    "transferId", event.transferId());

        } catch (Exception e) {
            log.error("Failed to send transfer failure notification",
                    "transferId", event.transferId());
        }
    }
}

