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
                        .secure(true)
                        .path("/")
                        .sameSite("Strict");

        if (maxAgeSeconds != null) {
            builder.maxAge(maxAgeSeconds);
        }

        return builder.build();
    }

    public ResponseCookie clearCookie(String cookieName) {

        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(0)
                .build();
    }
}