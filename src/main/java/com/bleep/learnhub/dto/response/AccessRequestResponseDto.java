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
public class AccessRequestResponseDto {
    private UUID id;
    private UUID partnerId;
    private UUID vendorId;
    private UUID courseId;
    private UUID batchId;
    private String partnerName;
    private String courseName;
    private String batchName;
    private String status;
    private String requestNote;
    private Boolean hasBatchAccess;
    private Integer maxStudents;
    private String responseNote;
    private String requestedAt;
    private String resolvedAt;
}
