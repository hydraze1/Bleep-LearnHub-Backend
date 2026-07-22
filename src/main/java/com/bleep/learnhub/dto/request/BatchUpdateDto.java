package com.bleep.learnhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BatchUpdateDto {

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
