package com.bleep.learnhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stores OTP session data in Redis during the password setup/reset flow.
 * Keyed by a random UUID token which is placed in the otp_session cookie.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpSessionData {

    private String username;
    private String email;
    private String otp;
}
