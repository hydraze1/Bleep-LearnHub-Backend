package com.bleep.learnhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for POST /api/v1/auth/set-password.
 * The user identity is resolved from the otp_session cookie, not from this payload,
 * so no username field is required here.
 * This single endpoint handles both first-time setup and forgot-password reset.
 */
@Data
public class SetPasswordRequestDto {

    @NotBlank(message = "OTP is required")
    private String otp;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;
}
