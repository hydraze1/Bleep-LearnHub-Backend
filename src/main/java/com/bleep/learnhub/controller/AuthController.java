package com.bleep.learnhub.controller;

import com.bleep.learnhub.constants.CookieConstants;
import com.bleep.learnhub.dto.request.ForgotUsernameRequestDto;
import com.bleep.learnhub.dto.request.LoginRequestDto;
import com.bleep.learnhub.dto.request.SendOtpRequestDto;
import com.bleep.learnhub.dto.request.SetPasswordRequestDto;
import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.dto.response.DeviceDetailsDto;
import com.bleep.learnhub.dto.response.LoginResponseDto;
import com.bleep.learnhub.dto.response.UserDataDto;
import com.bleep.learnhub.security.CookieService;
import com.bleep.learnhub.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles all authentication-related endpoints.
 *
 * <p>All session state is managed via HttpOnly cookies — no JWT is ever
 * exposed in a response body or required in request headers.
 *
 * <pre>
 * POST   /api/v1/auth/login             — authenticate, get session cookie
 * GET    /api/v1/auth/session           — validate session cookie, return profile
 * POST   /api/v1/auth/logout            — invalidate session cookie
 * POST   /api/v1/auth/send-otp         — send OTP email, get otp_session cookie
 * POST   /api/v1/auth/set-password     — verify OTP, set password (setup or reset)
 * POST   /api/v1/auth/forgot-username  — email the username to the registered address
 * </pre>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    // ── 1. Login ──────────────────────────────────────────────────────────────────

    /**
     * Authenticates the user and sets an HttpOnly {@code session_id} cookie.
     *
     * <p>Optional device headers are read and stored for audit purposes:
     * <ul>
     *   <li>{@code X-Device-Type} — e.g. "MOBILE", "WEB"</li>
     *   <li>{@code X-Device-Ip}   — client IP (also available via RemoteAddr)</li>
     *   <li>{@code X-Browser-Type}— e.g. "Chrome/124"</li>
     * </ul>
     *
     * <p>Response body shape:
     * <pre>
     * SUPER_ADMIN → { user }
     * VENDOR      → { user, vendor }
     * PARTNER     → { user, vendor, partner }
     * </pre>
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        String deviceIp = servletRequest.getHeader("Device-Ip");
        String resolvedIp = (deviceIp == null || deviceIp.isBlank()) ? servletRequest.getRemoteAddr() : deviceIp;

        DeviceDetailsDto deviceDetails = DeviceDetailsDto.builder()
                .deviceIp(resolvedIp)
                .deviceType(servletRequest.getHeader("Device-Type"))
                .device(servletRequest.getHeader("Device"))
                .deviceModel(servletRequest.getHeader("Device-Model"))
                .osName(servletRequest.getHeader("OS-Name"))
                .osVersion(servletRequest.getHeader("OS-Version"))
                .clientName(servletRequest.getHeader("Client-Name"))
                .clientVersion(servletRequest.getHeader("Client-Version"))
                .build();

        AuthService.LoginResult result = authService.login(request, deviceDetails);

        // Set the session cookie on the response (HttpOnly + Secure + SameSite=Strict)
        ResponseCookie sessionCookie = cookieService.createCookie(
                CookieConstants.SESSION_ID, result.sessionId(), CookieConstants.SESSION_AGE);
        servletResponse.addHeader("Set-Cookie", sessionCookie.toString());

        return ResponseEntity.ok(ApiResponse.success(result.data(), "Login successful."));
    }

    // ── 2. Session ────────────────────────────────────────────────────────────────

    /**
     * Validates the {@code session_id} cookie and returns the same profile payload
     * as login — served directly from Redis without a database hit.
     */
    @GetMapping("/session")
    public ResponseEntity<ApiResponse<LoginResponseDto>> session(HttpServletRequest request) {
        String sessionId = extractCookie(request, CookieConstants.SESSION_ID);

        if (sessionId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("No active session found. Please log in.", "401", "NO_SESSION"));
        }

        LoginResponseDto data = authService.getSession(sessionId);

        if (data == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Session has expired. Please log in again.", "401", "SESSION_EXPIRED"));
        }

        return ResponseEntity.ok(ApiResponse.success(data, "Session is active."));
    }

    // ── 3. Logout ─────────────────────────────────────────────────────────────────

    /**
     * Deletes the Redis session and clears the {@code session_id} cookie.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        String sessionId = extractCookie(request, CookieConstants.SESSION_ID);
        if (sessionId != null) {
            authService.logout(sessionId);
        }

        // Clear the cookie by setting max-age to 0
        ResponseCookie clearCookie = cookieService.clearCookie(CookieConstants.SESSION_ID);
        response.addHeader("Set-Cookie", clearCookie.toString());

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully."));
    }

    // ── 4. Send OTP ───────────────────────────────────────────────────────────────

    /**
     * Resolves the user by username or email, applies a 3-per-30-min rate limit,
     * sends a 6-digit OTP to the registered email, and sets an {@code otp_session}
     * cookie pointing to the OTP data in Redis.
     *
     * <p>Returns HTTP 429 if the rate limit is exceeded (via BusinessException → 400;
     * you may change to 429 in GlobalExceptionHandler if desired).
     */
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @Valid @RequestBody SendOtpRequestDto dto,
            HttpServletResponse response) {

        String otpToken = authService.sendOtp(dto.getUsernameOrEmail());

        // Set the short-lived otp_session cookie (30 min)
        ResponseCookie otpCookie = cookieService.createCookie(
                CookieConstants.OTP_SESSION, otpToken, CookieConstants.OTP_SESSION_AGE);
        response.addHeader("Set-Cookie", otpCookie.toString());

        return ResponseEntity.ok(ApiResponse.success("OTP sent to the registered email address."));
    }

    // ── 5. Set / Reset Password ───────────────────────────────────────────────────

    /**
     * Validates the OTP (using the {@code otp_session} cookie to find the Redis record)
     * and sets the new password. Handles both first-time account setup and forgot-password
     * resets with a single endpoint.
     *
     * <p>On success the {@code otp_session} cookie is cleared.
     */
    @PostMapping("/set-password")
    public ResponseEntity<ApiResponse<Void>> setPassword(
            @Valid @RequestBody SetPasswordRequestDto dto,
            HttpServletRequest request,
            HttpServletResponse response) {

        String otpToken = extractCookie(request, CookieConstants.OTP_SESSION);

        if (otpToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(
                            "OTP session not found. Please use 'send-otp' to request a new OTP.",
                            "400",
                            "OTP_SESSION_MISSING"));
        }

        authService.setPassword(otpToken, dto.getOtp(), dto.getNewPassword());

        // Clear the OTP session cookie — it is now consumed
        ResponseCookie clearCookie = cookieService.clearCookie(CookieConstants.OTP_SESSION);
        response.addHeader("Set-Cookie", clearCookie.toString());

        return ResponseEntity.ok(ApiResponse.success(
                "Password set successfully. You can now log in."));
    }

    // ── 6. Forgot Username ────────────────────────────────────────────────────────

    /**
     * Sends the username to the provided email address if an account exists.
     * Always returns HTTP 200 regardless of whether the email is registered,
     * to prevent email-enumeration attacks.
     */
    @PostMapping("/forgot-username")
    public ResponseEntity<ApiResponse<Void>> forgotUsername(
            @Valid @RequestBody ForgotUsernameRequestDto dto) {

        authService.forgotUsername(dto.getEmail());

        return ResponseEntity.ok(ApiResponse.success(
                "If this email is registered, your username has been sent to it."));
    }

    // ── 7. Get User List ──────────────────────────────────────────────────────────

    /**
     * Provides a list of all users in the users table without requiring authentication.
     */
    @GetMapping({"/users", "/user-list"})
    public ResponseEntity<ApiResponse<List<UserDataDto>>> getUserList() {
        List<UserDataDto> users = authService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users list retrieved successfully."));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    /**
     * Extracts the value of a named cookie from the request, or {@code null} if absent.
     */
    private String extractCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}