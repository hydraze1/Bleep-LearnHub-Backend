package com.bleep.learnhub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class SessionSequenceDto {
    @NotNull(message = "Session ID is required")
    private UUID id;

    @NotNull(message = "Sequence number is required")
    private Integer sequence;
}
