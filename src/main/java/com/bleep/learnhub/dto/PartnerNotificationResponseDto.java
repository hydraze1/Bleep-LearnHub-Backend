package com.bleep.learnhub.dto;

import com.bleep.learnhub.entity.enums.NotificationPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerNotificationResponseDto {
    private UUID recipientRecordId; // ID in notification_recipients
    private UUID notificationId;
    private String title;
    private String message;
    private UUID vendorId;
    private String vendorName;
    private NotificationPriority priority;
    private Boolean isRead;
    private LocalDateTime readAt;
    private Boolean delivered;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
}
