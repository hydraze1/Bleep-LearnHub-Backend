package com.bleep.learnhub.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VendorProfileResponseDto {
    private String id;
    private String username;
    private String email;
    private String companyName;
    private String phone;
    private String description;
    private String status; // ACTIVE, PENDING_SETUP, etc.
    private boolean isActive;
    private LocalDateTime joinedAt;
}