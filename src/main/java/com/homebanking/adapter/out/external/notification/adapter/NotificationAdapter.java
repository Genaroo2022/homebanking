package com.homebanking.adapter.out.external.notification.adapter;

import com.homebanking.domain.entity.Transfer;
import com.homebanking.port.out.NotificationOutputPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter: NotificationAdapter

 * Implementación del puerto NotificationOutputPort.
 * Mock implementation que simula envío de notificaciones.

 * En producción, sería reemplazado por implementación real que envíe:
 * • Email (mediante servicio de email)
 * • SMS (mediante Twilio, AWS SNS, etc.)
 * • Push notifications (Firebase, Apple, etc.)
 * • Webhooks (a aplicación cliente del usuario)

 * Características:
 * ✓ Desacoplado de lógica de negocio
 * ✓ Fire-and-forget (no bloquea el flujo principal)
 * ✓ Puede implementarse de forma asincrónica
 * ✓ Observable: logging detallado
 * ✓ Resiliente: fallos en notificaciones no afectan la transferencia
 */
@Component
@Slf4j
public class NotificationAdapter implements NotificationOutputPort {

    /**
     * Notifica que una transferencia fue completada.

     * En producción, aquí iría:
     * • Enviar email al usuario
     * • Publicar evento en cola de mensajes (Kafka, RabbitMQ)
     * • Llamar webhook del cliente
     * • Crear notificación en BD para mostrar en app
     */
    @Override
    public void notifyTransferCompleted(Transfer transfer) {
        log.info("NOTIFICACIÓN - Transferencia completada: " +
                        "id={}, amount={}, targetCbu={}, timestamp={}",
                transfer.getId(),
                transfer.getAmount(),
                transfer.getTargetCbu(),
                transfer.getExecutedAt());

        try {
            // Simular envío de notificación (en producción, llamar servicio real)
            sendEmailNotification(
                    "transferencia_completada@banco.com",
                    transfer
            );

            // Simular publicación de evento en cola de mensajes
            publishTransferCompletedEvent(transfer);

            log.debug("Notificación enviada exitosamente para transferencia: id={}",
                    transfer.getId());

        } catch (Exception ex) {
            // NO fallar la transferencia si la notificación falla
            // Esto es crítico: la transferencia se completó, solo la notificación falló
            log.error("Error enviando notificación de transferencia completada: id={}, error={}",
                    transfer.getId(), ex.getMessage(), ex);

            // Aquí podríamos agendar reintento de notificación si fuera importante
        }
    }

    /**
     * Notifica que una transferencia falló.
     */
    @Override
    public void notifyTransferFailed(Transfer transfer) {
        log.warn("NOTIFICACIÓN - Transferencia fallida: " +
                        "id={}, amount={}, reason={}, timestamp={}",
                transfer.getId(),
                transfer.getAmount(),
                transfer.getFailureReason(),
                transfer.getFailedAt());

        try {
            sendEmailNotification(
                    "transferencia_fallida@banco.com",
                    transfer
            );

            publishTransferFailedEvent(transfer);

            log.debug("Notificación de fallo enviada para transferencia: id={}",
                    transfer.getId());

        } catch (Exception ex) {
            log.error("Error enviando notificación de transferencia fallida: id={}, error={}",
                    transfer.getId(), ex.getMessage(), ex);
        }
    }

    /**
     * Simula envío de email.
     * En producción: usar SendGrid, Mailgun, AWS SES, etc.
     */
    private void sendEmailNotification(String templateType, Transfer transfer) {
        log.debug("Enviando email [{}] para transferencia: id={}",
                templateType, transfer.getId());

        // Simular latencia de envío
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.debug("Email enviado exitosamente");
    }

    /**
     * Publica evento en sistema de mensajería.
     * En producción: usar Kafka, RabbitMQ, AWS SNS, etc.
     */
    private void publishTransferCompletedEvent(Transfer transfer) {
        log.debug("Publicando evento TransferCompleted a cola de mensajes: id={}",
                transfer.getId());

        // En producción, el evento iría a Kafka/RabbitMQ
        // Otros servicios (notificaciones, auditoría, analytics) lo consumirían

        log.debug("Evento publicado exitosamente");
    }

    /**
     * Publica evento de transferencia fallida.
     */
    private void publishTransferFailedEvent(Transfer transfer) {
        log.debug("Publicando evento TransferFailed a cola de mensajes: id={}",
                transfer.getId());

        log.debug("Evento publicado exitosamente");
    }
}