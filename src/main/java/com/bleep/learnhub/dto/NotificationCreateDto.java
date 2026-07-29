package com.bleep.learnhub.dto;

import com.bleep.learnhub.entity.enums.NotificationPriority;
import com.bleep.learnhub.entity.enums.NotificationTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationCreateDto {

    @NotBlank(message = "Notification title is required")
    private String title;

    @NotBlank(message = "Notification message is required")
    private String message;

    @NotNull(message = "Notification target type is required")
    private NotificationTargetType targetType;

    private UUID targetEntityId; // courseId or batchId

    private List<UUID> recipientPartnerIds; // used if targetType is SPECIFIC_PARTNERS

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;
}
