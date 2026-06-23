package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.request.LoginRequestDto;
import com.bleep.learnhub.dto.request.SetPasswordDto;
import com.bleep.learnhub.dto.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<Void>> sendOtp(@RequestParam String username) {
        authService.generateAndSendOtp(username);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully to registered email."));
    }

    // 2. User verifies OTP and sets their first password
    @PostMapping("/set-password")
    public ResponseEntity<ApiResponse<Void>> setPassword(@RequestBody SetPasswordDto dto) {
        authService.verifyOtpAndSetPassword(dto.getUsername(), dto.getOtp(), dto.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password set successfully. You can now log in."));
    }

    // 3. Standard Login returning a JWT
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequestDto loginRequest) {
        // Authenticates user, creates session in Redis, returns JWT
        String token = authService.authenticateAndGenerateToken(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(token, "Login successful."));
    }
    
    // 4. Logout (Blacklists JWT or removes session from Redis)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        authService.logoutUser(token);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully."));
    }
}