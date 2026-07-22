package com.bleep.learnhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class PartnerAccessRequestCreateDto {
    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @NotBlank(message = "Course Name is required")
    private String courseName;

    private UUID batchId;

    private String batchName;

    @NotNull(message = "Partner ID is required")
    private UUID partnerId;

    @NotBlank(message = "Partner Name is required")
    private String partnerName;

    @NotNull(message = "Max Students is required")
    private Integer maxStudents;

    private String note; // request note (optional)
}
