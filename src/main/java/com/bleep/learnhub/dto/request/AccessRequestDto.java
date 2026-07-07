package com.bleep.learnhub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AccessRequestDto {

    @NotNull(message = "Vendor ID is required")
    private UUID vendorId;

    @NotNull(message = "Course ID is required")
    private UUID courseId;

    private UUID batchId;

    private String requestNote;
}
