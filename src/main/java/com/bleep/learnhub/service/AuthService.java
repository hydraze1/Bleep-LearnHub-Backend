package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.request.LoginRequestDto;
import com.bleep.learnhub.entity.User;
import com.bleep.learnhub.entity.enums.AccountStatus;
import com.bleep.learnhub.repository.UserRepository;
import com.bleep.learnhub.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    // Assuming you have a RedisService and a JwtUtil (or JwtService) class created
    private final RedisService redisService; 
    private final JwtService jwtService; 

    public void generateAndSendOtp(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate a cryptographically secure 6-digit OTP
        SecureRandom random = new SecureRandom();
        String otp = String.valueOf(100000 + random.nextInt(900000));

        // Save OTP to Redis with a 5-minute TTL
        redisService.saveOtp(username, otp);

        // Send Email
        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    @Transactional
    public void verifyOtpAndSetPassword(String username, String otp, String newPassword) {
        // 1. Validate OTP from Redis
        if (!redisService.validateOtp(username, otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // 2. Update User Record
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        userRepository.save(user);

        // 3. Delete OTP from Redis so it cannot be reused
        redisService.deleteOtp(username);
    }

    @Transactional
    public String authenticateAndGenerateToken(LoginRequestDto request) {
        // 1. Authenticate credentials via Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active. Please complete setup or contact support.");
        }

        // 2. Generate JWT Token
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        String sessionId = jwtService.extractSessionId(token); // Assuming JWT contains a custom "jti" claim

        // 3. Save active session to Redis for quick validation
        redisService.saveSession(sessionId, user.getUsername(), 7); // 7 days TTL

        // (Optional) Log to PostgreSQL user_sessions table here for permanent audit trail

        return token;
    }

    public void logoutUser(String token) {
        // Extract session ID from token and remove it from Redis
        String sessionId = jwtService.extractSessionId(token);
        redisService.deleteSession(sessionId);
        
        // (Optional) Mark session as inactive in PostgreSQL here
    }
}