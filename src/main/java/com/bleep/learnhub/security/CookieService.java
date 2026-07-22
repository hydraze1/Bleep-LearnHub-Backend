package com.bleep.learnhub.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public ResponseCookie createCookie(
            String cookieName,
            String value,
            Long maxAgeSeconds
    ) {

        ResponseCookie.ResponseCookieBuilder builder =
                ResponseCookie.from(cookieName, value)
                        .httpOnly(true)
                        .secure(false)   // set to true when using HTTPS in production
                        .path("/")
                        .sameSite("Lax"); // Lax allows cross-origin requests with cookies

        if (maxAgeSeconds != null) {
            builder.maxAge(maxAgeSeconds);
        }

        return builder.build();
    }

    public ResponseCookie clearCookie(String cookieName) {

        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(false)   // match the createCookie setting
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
    }
}