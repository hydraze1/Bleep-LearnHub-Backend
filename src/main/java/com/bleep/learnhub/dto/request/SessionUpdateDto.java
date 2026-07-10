package com.bleep.learnhub.dto.request;

import com.bleep.learnhub.entity.enums.SessionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SessionUpdateDto {

    @NotNull(message = "Session Type is required")
    private SessionType sessionType;

    @NotBlank(message = "Title is required")
    private String title;

    private String subtitle;

    @NotBlank(message = "Description is required")
    private String description;

    private String liveLink;
    private String recordedLink;
    private String resourceLink;

    @NotNull(message = "Sequence order is required")
    private Integer sequenceOrder;

    private LocalDate scheduledDate;

    @jakarta.validation.constraints.Pattern(regexp = "^(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$", message = "wrong format need in 24hr format time")
    private String scheduledTime;
}
