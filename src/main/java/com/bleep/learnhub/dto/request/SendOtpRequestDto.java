package com.bleep.learnhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for POST /api/v1/auth/send-otp.
 * Accepts either a username or an email address.
 */
@Data
public class SendOtpRequestDto {

    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;
}
