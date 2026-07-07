package com.bleep.learnhub.dto.request;

import com.bleep.learnhub.entity.enums.AccessRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccessStatusUpdateDto {

    @NotNull(message = "Status is required")
    private AccessRequestStatus status;

    private String responseNote;
}
