package com.bleep.learnhub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String OTP_PREFIX = "otp:";
    private static final String SESSION_PREFIX = "session:";

    public void saveOtp(String username, String otp) {
        redisTemplate.opsForValue().set(OTP_PREFIX + username, otp, 5, TimeUnit.MINUTES);
    }

    public boolean validateOtp(String username, String otp) {
        String savedOtp = (String) redisTemplate.opsForValue().get(OTP_PREFIX + username);
        return otp != null && otp.equals(savedOtp);
    }

    public void deleteOtp(String username) {
        redisTemplate.delete(OTP_PREFIX + username);
    }

    public void saveSession(String sessionId, String username, int days) {
        redisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, username, days, TimeUnit.DAYS);
    }

    public boolean isSessionActive(String sessionId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(SESSION_PREFIX + sessionId));
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(SESSION_PREFIX + sessionId);
    }
}
