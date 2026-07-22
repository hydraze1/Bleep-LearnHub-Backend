package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.OtpSessionData;
import com.bleep.learnhub.dto.response.LoginResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${app.otp.time-frame-minutes}")
    private int otpTimeFrameMinutes;

    // ── Key Prefixes ────────────────────────────────────────────────────────────

    /** Stores the full session payload (LoginResponseDto as JSON). */
    private static final String SESSION_PREFIX    = "session:";

    /** Stores OTP session data during password setup/reset flow. */
    private static final String OTP_SESSION_PREFIX = "otp_session:";

    /** Stores OTP send-count per user for rate limiting (max 3 per 30 min). */
    private static final String OTP_COUNT_PREFIX   = "otp_count:";

    // ── Session ──────────────────────────────────────────────────────────────────

    /**
     * Serialises {@code data} to JSON and stores it at {@code session:{sessionId}}.
     *
     * @param sessionId UUID string used as the Redis key suffix and as the cookie value.
     * @param data      The full login payload for the session.
     * @param days      TTL in days.
     */
    public void saveSessionData(String sessionId, LoginResponseDto data, int days) {
        try {
            String json = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, json, days, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialise session data for sessionId={}: {}", sessionId, e.getMessage());
            throw new RuntimeException("Failed to save session data", e);
        }
    }

    /**
     * Fetches and deserialises session data from Redis.
     *
     * @return The {@link LoginResponseDto} or {@code null} if the key does not exist.
     */
    public LoginResponseDto getSessionData(String sessionId) {
        Object raw = redisTemplate.opsForValue().get(SESSION_PREFIX + sessionId);
        if (raw == null) return null;
        try {
            return objectMapper.readValue(raw.toString(), LoginResponseDto.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialise session data for sessionId={}: {}", sessionId, e.getMessage());
            return null;
        }
    }

    /**
     * Deletes the session key from Redis (used on logout).
     */
    public void deleteSession(String sessionId) {
        redisTemplate.delete(SESSION_PREFIX + sessionId);
    }

    // ── OTP Session (for password setup / reset) ─────────────────────────────────

    /**
     * Stores OTP session data at {@code otp_session:{token}} with a 30-minute TTL.
     *
     * @param token The UUID token placed in the otp_session cookie.
     * @param data  The OTP session data (username, email, otp).
     */
    public void saveOtpSession(String token, OtpSessionData data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(OTP_SESSION_PREFIX + token, json, otpTimeFrameMinutes, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialise OTP session for token={}: {}", token, e.getMessage());
            throw new RuntimeException("Failed to save OTP session", e);
        }
    }

    /**
     * Fetches and deserialises OTP session data from Redis.
     *
     * @return The {@link OtpSessionData} or {@code null} if expired / not found.
     */
    public OtpSessionData getOtpSession(String token) {
        Object raw = redisTemplate.opsForValue().get(OTP_SESSION_PREFIX + token);
        if (raw == null) return null;
        try {
            return objectMapper.readValue(raw.toString(), OtpSessionData.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialise OTP session for token={}: {}", token, e.getMessage());
            return null;
        }
    }

    /**
     * Deletes the OTP session key after successful password set (prevents re-use).
     */
    public void deleteOtpSession(String token) {
        redisTemplate.delete(OTP_SESSION_PREFIX + token);
    }

    // ── OTP Rate Limiting ─────────────────────────────────────────────────────────

    /**
     * Retrieves how many OTPs have been sent for a user in the current 30-minute window.
     */
    public int getOtpCount(String username) {
        Object val = redisTemplate.opsForValue().get(OTP_COUNT_PREFIX + username);
        if (val == null) return 0;
        try {
            return Integer.parseInt(val.toString().replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Atomically increments the OTP send-count for a user and sets a 30-minute TTL
     * on the very first increment (window start).
     *
     * @return The new count after incrementing.
     */
    public int incrementOtpCount(String username) {
        String key = OTP_COUNT_PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            // First OTP in this window — start the expiry clock
            redisTemplate.expire(key, otpTimeFrameMinutes, TimeUnit.MINUTES);
        }
        return count != null ? count.intValue() : 1;
    }

    // ── Legacy OTP (kept for backward compatibility) ──────────────────────────────

    /** @deprecated Use {@link #saveOtpSession} / {@link #getOtpSession} instead. */
    @Deprecated(forRemoval = true)
    public void saveOtp(String username, String otp) {
        redisTemplate.opsForValue().set("otp:" + username, otp, 5, TimeUnit.MINUTES);
    }

    /** @deprecated Use {@link #saveOtpSession} / {@link #getOtpSession} instead. */
    @Deprecated(forRemoval = true)
    public boolean validateOtp(String username, String otp) {
        String savedOtp = (String) redisTemplate.opsForValue().get("otp:" + username);
        return otp != null && otp.equals(savedOtp);
    }

    /** @deprecated Use {@link #deleteOtpSession} instead. */
    @Deprecated(forRemoval = true)
    public void deleteOtp(String username) {
        redisTemplate.delete("otp:" + username);
    }

    // ── Global API Rate Limiting ──────────────────────────────────────────────────
    
    private static final String API_RATE_LIMIT_PREFIX = "rate_limit:";
    
    /**
     * Increments the API call count for an IP or User and returns true if allowed.
     * @param identifier The IP address or username
     * @param maxRequests Maximum allowed requests per window
     * @param windowMinutes Time window in minutes
     */
    public boolean allowApiCall(String identifier, int maxRequests, int windowMinutes) {
        String key = API_RATE_LIMIT_PREFIX + identifier;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, windowMinutes, TimeUnit.MINUTES);
        }
        return count == null || count <= maxRequests;
    }
}
