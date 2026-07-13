package com.bleep.learnhub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class SessionReorderRequestDto {
    
    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @NotNull(message = "Batch ID is required")
    private UUID batchId;

    @NotNull(message = "Session IDs are required")
    private List<SessionOrderItem> sessionIds;

    @Data
    public static class SessionOrderItem {
        @NotNull(message = "Index is required")
        private Integer index;
        
        @NotNull(message = "Session ID is required")
        private UUID id;
    }
}
