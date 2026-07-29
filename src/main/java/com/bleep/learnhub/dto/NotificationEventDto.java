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
public class NotificationEventDto {
    private UUID notificationId;
    private String title;
    private String message;
    private String vendorName;
    private NotificationPriority priority;
    private LocalDateTime createdAt;
}
