package com.homebanking.adapter.out.external.notification.adapter;

import com.homebanking.domain.event.TransferCompletedEvent;
import com.homebanking.domain.event.TransferFailedEvent;
import com.homebanking.port.out.notification.NotificationOutputPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class NotificationAdapter implements NotificationOutputPort {

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${notification.push.enabled:false}")
    private boolean pushEnabled;

    @Override
    public void notifyTransferCompleted(TransferCompletedEvent event) {
        log.info("NOTIFICATION transfer_completed id={} amount={} targetCbu={} timestamp={}",
                event.transferId(),
                event.amount(),
                event.targetCbu(),
                event.completedAt());
        dispatch("transfer_completed", event.transferId());
    }

    @Override
    public void notifyTransferFailed(TransferFailedEvent event) {
        log.warn("NOTIFICATION transfer_failed id={} amount={} reason={} timestamp={}",
                event.transferId(),
                event.amount(),
                event.failureReason(),
                event.failedAt());
        dispatch("transfer_failed", event.transferId());
    }

    private void dispatch(String templateType, UUID transferId) {
        try {
            if (emailEnabled) {
                sendEmail(templateType, transferId);
            }
            if (smsEnabled) {
                sendSms(templateType, transferId);
            }
            if (pushEnabled) {
                sendPush(templateType, transferId);
            }
        } catch (Exception ex) {
            // Best-effort notifications should not fail transfer lifecycle.
            log.error("Notification dispatch failed transferId={} template={} error={}",
                    transferId, templateType, ex.getMessage(), ex);
        }
    }

    private void sendEmail(String templateType, UUID transferId) {
        log.debug("Sending email notification template={} transferId={}", templateType, transferId);
    }

    private void sendSms(String templateType, UUID transferId) {
        log.debug("Sending sms notification template={} transferId={}", templateType, transferId);
    }

    private void sendPush(String templateType, UUID transferId) {
        log.debug("Sending push notification template={} transferId={}", templateType, transferId);
    }
}

