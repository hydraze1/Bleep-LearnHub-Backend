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
public class PartnerAccessSessionDto {
    private UUID id;
    private UUID courseId;
    private String courseName;
    private UUID batchId;
    private String batchName;
    private String title;
    private String subtitle;
    private String description;
    private String sessionType;
    private Integer sequenceOrder;
    private String scheduledDate;
    private String scheduledTime;
    
    // Links only present if approved
    private String liveLink;
    private String recordedLink;
    private String resourceLink;

    private boolean hasRequestedAccess;
    private String accessStatus;
}
