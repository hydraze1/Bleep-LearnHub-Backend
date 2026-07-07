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
public class CourseDataDto {
    private UUID id;
    private String title;
    private String subtitle;
    private String description;
    private String category;
    private String createdAt;
    private String updatedAt;
}
