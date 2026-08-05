package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.NotificationEventDto;
import com.bleep.learnhub.entity.Notification;
import com.bleep.learnhub.event.NotificationCreatedEvent;
import com.bleep.learnhub.sse.SseConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSender {

    private final SseConnectionManager connectionManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationCreatedEvent(NotificationCreatedEvent event) {
        log.info("🔔 DB Transaction committed successfully. Triggering real-time SSE push for notification ID: {}", 
                event.getNotification().getId());
        sendToRecipients(event.getNotification(), event.getRecipientPartnerIds());
    }

    public void sendToRecipients(Notification notification, List<UUID> recipientPartnerIds) {
        NotificationEventDto event = NotificationEventDto.builder()
                .notificationId(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .vendorName(notification.getVendor().getCompanyName())
                .priority(notification.getPriority())
                .createdAt(notification.getCreatedAt())
                .build();

        for (UUID partnerId : recipientPartnerIds) {
            if (connectionManager.isConnected(partnerId)) {
                log.info("Sending SSE notification to connected partner: {}", partnerId);
                connectionManager.send(partnerId, event, "notification");
            } else {
                log.info("Partner {} is offline. Skipping real-time SSE push.", partnerId);
            }
        }
    }
}
