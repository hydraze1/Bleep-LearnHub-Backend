package com.bleep.learnhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BatchUpdateDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String subtitle;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Scheduled Date is required")
    private LocalDate scheduledDate;

    @NotNull(message = "Scheduled Time is required")
    @jakarta.validation.constraints.Pattern(regexp = "^(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$", message = "wrong format need in 24hr format time")
    private String scheduledTime;
}
