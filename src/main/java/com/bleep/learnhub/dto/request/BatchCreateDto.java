package com.bleep.learnhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BatchCreateDto {

    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @NotBlank(message = "Title is required")
    private String title;

    private String subtitle;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Starting Date is required")
    private LocalDate startingDate;

    @NotNull(message = "Ending Date is required")
    private LocalDate endingDate;
}
