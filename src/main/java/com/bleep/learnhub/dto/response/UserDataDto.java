package com.bleep.learnhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Flat representation of the User entity row.
 * Excludes sensitive fields such as passwordHash.
 * Used as part of LoginResponseDto and cached in Redis sessions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataDto {

    private String id;
    private String username;
    private String email;
    private String role;
    private String status;
    private boolean emailVerified;
    private String lastLoginAt;
    private String createdAt;
    private String updatedAt;
    private DeviceDetailsDto deviceDetails;
}
