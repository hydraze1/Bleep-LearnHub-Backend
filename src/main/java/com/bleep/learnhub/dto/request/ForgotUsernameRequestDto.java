package com.bleep.learnhub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for POST /api/v1/auth/forgot-username.
 * The server looks up the user by email and sends the username to that address.
 * Always returns HTTP 200 to prevent email enumeration.
 */
@Data
public class ForgotUsernameRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
}
