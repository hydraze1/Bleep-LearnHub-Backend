package com.bleep.learnhub.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDto {
    private String accessToken;
    private String role; // VENDOR, PARTNER, SUPER_ADMIN
    private String username;
}