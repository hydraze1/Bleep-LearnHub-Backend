package com.bleep.learnhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerCalendarSessionDto {
    private UUID sessionId;
    private UUID courseId;
    private String courseName;
    private UUID batchId;
    private String batchName;
    private String title;
    private String subtitle;
    private String description;
    private Integer sequenceOrder;
    private String scheduledDate;
    private String scheduledTime;
}
