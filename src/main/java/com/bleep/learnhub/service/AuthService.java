package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.OtpSessionData;
import com.bleep.learnhub.dto.request.LoginRequestDto;
import com.bleep.learnhub.dto.response.LoginResponseDto;
import com.bleep.learnhub.dto.response.PartnerDataDto;
import com.bleep.learnhub.dto.response.UserDataDto;
import com.bleep.learnhub.dto.response.VendorDataDto;
import com.bleep.learnhub.entity.Partner;
import com.bleep.learnhub.entity.User;
import com.bleep.learnhub.entity.UserSession;
import com.bleep.learnhub.entity.Vendor;
import com.bleep.learnhub.entity.enums.AccountStatus;
import com.bleep.learnhub.entity.enums.Role;
import com.bleep.learnhub.exception.BusinessException;
import com.bleep.learnhub.exception.ResourceNotFoundException;
import com.bleep.learnhub.repository.PartnerRepository;
import com.bleep.learnhub.repository.UserRepository;
import com.bleep.learnhub.repository.UserSessionRepository;
import com.bleep.learnhub.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final PartnerRepository partnerRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final RedisService redisService;

    // ── Result record for login (sessionId + payload) ────────────────────────────

    /**
     * Returned by {@link #login} so the controller can set the cookie and the body
     * separately without the service touching {@code HttpServletResponse}.
     */
    public record LoginResult(String sessionId, LoginResponseDto data) {}

    // ── Login ────────────────────────────────────────────────────────────────────

    /**
     * Authenticates the user, builds the composite {@link LoginResponseDto},
     * saves it to Redis, saves an audit record to {@code user_sessions}, and
     * returns the session ID for the caller to use as a cookie value.
     *
     * @param request     Login credentials (username + password).
     * @param deviceType  Optional — e.g. "MOBILE", "WEB".
     * @param deviceIp    Optional — client IP address.
     * @param browserType Optional — e.g. "Chrome/124".
     */
    @Transactional
    public LoginResult login(LoginRequestDto request,
                             String deviceType,
                             String deviceIp,
                             String browserType) {

        // 1. Authenticate — throws BadCredentialsException, DisabledException, LockedException
        //    which are all handled by GlobalExceptionHandler.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. Re-fetch the User entity (authentication passed, so we know it exists)
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Account is not active. Please complete setup or contact support.");
        }

        // 3. Build the composite response DTO
        LoginResponseDto responseDto = buildLoginResponseDto(user);

        // 4. Generate session ID and persist in Redis (TTL = 7 days)
        String sessionId = UUID.randomUUID().toString();
        redisService.saveSessionData(sessionId, responseDto, 7);

        // 5. Audit: persist session record in PostgreSQL for history/reporting
        UserSession sessionRecord = UserSession.builder()
                .user(user)
                .sessionId(sessionId)
                .deviceType(deviceType)
                .ipAddress(deviceIp)
                .browser(browserType)
                .isActive(true)
                .build();
        userSessionRepository.save(sessionRecord);

        // 6. Update last login timestamp
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return new LoginResult(sessionId, responseDto);
    }

    // ── Session ──────────────────────────────────────────────────────────────────

    /**
     * Fetches the cached session payload from Redis without touching the database.
     *
     * @param sessionId The value of the {@code session_id} cookie.
     * @return The {@link LoginResponseDto} or {@code null} if the session has expired.
     */
    public LoginResponseDto getSession(String sessionId) {
        return redisService.getSessionData(sessionId);
    }

    // ── Logout ───────────────────────────────────────────────────────────────────

    /**
     * Removes the session from Redis and marks it as inactive in the database.
     *
     * @param sessionId The value of the {@code session_id} cookie.
     */
    @Transactional
    public void logout(String sessionId) {
        // Remove from Redis — the next request with this cookie will be rejected
        redisService.deleteSession(sessionId);

        // Mark DB record as inactive for audit trail
        userSessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.setActive(false);
            session.setLogoutAt(LocalDateTime.now());
            userSessionRepository.save(session);
        });
    }

    // ── Send OTP ─────────────────────────────────────────────────────────────────

    /**
     * Resolves a user by username or email, enforces the 3-per-30-min rate limit,
     * generates a 6-digit OTP, stores it in Redis under a random token, and sends
     * it by email.
     *
     * @param usernameOrEmail The username or email address supplied by the caller.
     * @return The OTP session token (to be set as the {@code otp_session} cookie).
     */
    public String sendOtp(String usernameOrEmail) {
        // Resolve by username first, fall back to email
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found with this username or email"));

        // Rate-limit check (max 3 OTPs per 30-minute window per user)
        int currentCount = redisService.getOtpCount(user.getUsername());
        if (currentCount >= 3) {
            throw new BusinessException(
                    "OTP limit reached. You can only request 3 OTPs every 30 minutes. Please try again later.");
        }

        // Generate a cryptographically secure 6-digit OTP
        String otp = String.valueOf(100000 + new SecureRandom().nextInt(900000));

        // Generate a random token that acts as the Redis key and goes in the cookie
        String otpToken = UUID.randomUUID().toString();

        // Persist OTP session in Redis (TTL = 30 min)
        OtpSessionData otpData = OtpSessionData.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .otp(otp)
                .build();
        redisService.saveOtpSession(otpToken, otpData);

        // Increment rate-limit counter
        redisService.incrementOtpCount(user.getUsername());

        // Send OTP by email (async — will not block the response)
        emailService.sendOtpEmail(user.getEmail(), otp);

        return otpToken;
    }

    // ── Set / Reset Password ─────────────────────────────────────────────────────

    /**
     * Validates the OTP from the Redis session identified by {@code otpToken} and,
     * if correct, sets the new password and activates the account.
     * Works for both first-time setup and forgot-password resets.
     *
     * @param otpToken   The value of the {@code otp_session} cookie.
     * @param otp        The 6-digit OTP entered by the user.
     * @param newPassword The new password to set.
     */
    @Transactional
    public void setPassword(String otpToken, String otp, String newPassword) {
        // Fetch OTP session data from Redis
        OtpSessionData otpData = redisService.getOtpSession(otpToken);
        if (otpData == null) {
            throw new BusinessException(
                    "OTP session has expired or is invalid. Please request a new OTP.");
        }

        // Validate OTP
        if (!otp.equals(otpData.getOtp())) {
            throw new BusinessException("The OTP you entered is incorrect. Please check and try again.");
        }

        // Update user record
        User user = userRepository.findByUsername(otpData.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        userRepository.save(user);

        // Delete OTP session so it cannot be reused
        redisService.deleteOtpSession(otpToken);
    }

    // ── Forgot Username ──────────────────────────────────────────────────────────

    /**
     * Sends the username to the provided email address if a matching account exists.
     * Always returns silently (no exception) to prevent email-enumeration attacks.
     *
     * @param email The email address supplied by the caller.
     */
    public void forgotUsername(String email) {
        userRepository.findByEmail(email)
                .ifPresent(user -> emailService.sendForgotUsernameEmail(user.getEmail(), user.getUsername()));
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    /**
     * Builds the composite {@link LoginResponseDto} for a user, loading the Vendor
     * or Partner profile rows based on the user's role.
     */
    private LoginResponseDto buildLoginResponseDto(User user) {
        UserDataDto userDto = mapUserToDto(user);
        VendorDataDto vendorDto = null;
        PartnerDataDto partnerDto = null;

        if (user.getRole() == Role.VENDOR) {
            Vendor vendor = vendorRepository.findByUserUsername(user.getUsername()).orElse(null);
            if (vendor != null) {
                vendorDto = mapVendorToDto(vendor);
            }
        } else if (user.getRole() == Role.PARTNER) {
            Partner partner = partnerRepository.findByUserUsername(user.getUsername()).orElse(null);
            if (partner != null) {
                partnerDto = mapPartnerToDto(partner);
                // Also include the parent vendor so the frontend has full context
                vendorDto = mapVendorToDto(partner.getVendor());
            }
        }

        return LoginResponseDto.builder()
                .user(userDto)
                .vendor(vendorDto)
                .partner(partnerDto)
                .build();
    }

    private UserDataDto mapUserToDto(User user) {
        return UserDataDto.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null)
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null)
                .build();
    }

    private VendorDataDto mapVendorToDto(Vendor vendor) {
        return VendorDataDto.builder()
                .id(vendor.getId().toString())
                .companyName(vendor.getCompanyName())
                .phone(vendor.getPhone())
                .description(vendor.getDescription())
                .active(vendor.isActive())
                .createdAt(vendor.getCreatedAt() != null ? vendor.getCreatedAt().toString() : null)
                .build();
    }

    private PartnerDataDto mapPartnerToDto(Partner partner) {
        return PartnerDataDto.builder()
                .id(partner.getId().toString())
                .companyName(partner.getCompanyName())
                .phone(partner.getPhone())
                .description(partner.getDescription())
                .active(partner.isActive())
                .vendorId(partner.getVendor().getId().toString())
                .createdAt(partner.getCreatedAt() != null ? partner.getCreatedAt().toString() : null)
                .build();
    }
}