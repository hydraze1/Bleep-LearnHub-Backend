package com.bleep.learnhub.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class PartnerSessionResponseDto {
    private UUID id;
    private UUID courseId;
    private String courseName;
    private UUID batchId;
    private String batchName;
    private String sessionType;
    private String title;
    private String subtitle;
    private String description;
    private String liveLink;
    private String recordedLink;
    private String resourceLink;
    private Integer sequenceOrder;
    private String scheduledDate;
    private String scheduledTime;
    private long studentCount; // Number of unique students from the partner who accessed this session
    private Integer maxStudents; // Student limit from the partner access request
    private boolean limitExceeded; // true if studentCount > maxStudents
}
