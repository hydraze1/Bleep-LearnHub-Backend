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
public class BatchProfileResponseDto {
    private UUID id;
    private UUID courseId;
    private String title;
    private String subtitle;
    private String description;
    private String startingDate;
    private String endingDate;
    private String createdAt;
    private String updatedAt;
}
