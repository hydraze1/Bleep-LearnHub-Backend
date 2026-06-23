package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.request.LoginRequestDto;
import com.bleep.learnhub.dto.request.SetPasswordDto;
import com.bleep.learnhub.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 1. User enters username, system sends OTP to email via Redis
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestParam String username) {
        authService.generateAndSendOtp(username);
        return ResponseEntity.ok("OTP sent successfully to registered email.");
    }

    // 2. User verifies OTP and sets their first password
    @PostMapping("/set-password")
    public ResponseEntity<String> setPassword(@RequestBody SetPasswordDto dto) {
        authService.verifyOtpAndSetPassword(dto.getUsername(), dto.getOtp(), dto.getNewPassword());
        return ResponseEntity.ok("Password set successfully. You can now log in.");
    }

    // 3. Standard Login returning a JWT
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginRequest) {
        // Authenticates user, creates session in Redis, returns JWT
        String token = authService.authenticateAndGenerateToken(loginRequest);
        return ResponseEntity.ok(token);
    }
    
    // 4. Logout (Blacklists JWT or removes session from Redis)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        authService.logoutUser(token);
        return ResponseEntity.ok("Logged out successfully.");
    }
}