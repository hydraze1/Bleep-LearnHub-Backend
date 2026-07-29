package com.bleep.learnhub.dto;

import com.bleep.learnhub.entity.enums.NotificationPriority;
import com.bleep.learnhub.entity.enums.NotificationTargetType;
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
public class VendorNotificationResponseDto {
    private UUID id;
    private UUID vendorId;
    private String vendorCompanyName;
    private String title;
    private String message;
    private NotificationTargetType targetType;
    private UUID targetEntityId;
    private String targetEntityName; // course title or batch title
    private NotificationPriority priority;
    private long totalRecipients;
    private long readCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
