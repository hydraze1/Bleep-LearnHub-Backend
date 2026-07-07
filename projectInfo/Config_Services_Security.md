# Bleep LearnHub — Configuration, Services & Security Documentation

This document provides a detailed explanation of every file in the `config/`, `service/`, and `security/` packages — what each file does, why it exists, and what it contains.

---

## Table of Contents

**Configuration (`config/`)**
1. [SecurityConfig.java](#1-securityconfigjava)
2. [MasterLoggingFilter.java](#2-masterloggingfilterjava)
3. [RedisConfig.java](#3-redisconfigjava)
4. [MailConfig.java](#4-mailconfigjava)
5. [OpenApiConfig.java](#5-openapiconfigjava)
6. [AdminSeeder.java](#6-adminseederjava)
7. [LayerLoggingAspect.java](#7-layerloggingaspectjava-disabled)
8. [RequestTraceFilter.java](#8-requesttracefilterjava-superseded)

**Services (`service/`)**
9. [AuthService.java](#9-authservicejava)
10. [RedisService.java](#10-redisservicejava)
11. [EmailService.java](#11-emailservicejava)
12. [VendorService.java](#12-vendorservicejava)
13. [PartnerService.java](#13-partnerservicejava)
14. [AdminService.java](#14-adminservicejava)

**Security (`security/`)**
15. [JwtAuthenticationFilter.java](#15-jwtauthenticationfilterjava)
16. [JwtService.java](#16-jwtservicejava)
17. [CustomUserDetailsService.java](#17-customuserdetailsservicejava)
18. [UserPrincipal.java](#18-userprincipaljava)
19. [CookieService.java](#19-cookieservicejava)

---

# Configuration (`config/`)

The config package holds Spring `@Configuration` classes, servlet filters, and startup runners that wire up the application's infrastructure.

---

## 1. SecurityConfig.java

**File:** `config/SecurityConfig.java`  
**Type:** `@Configuration` + `@EnableWebSecurity` + `@EnableMethodSecurity`

### Purpose
The central Spring Security configuration. It defines **who can access what** and **how requests are authenticated**.

### What It Does
1. **Disables CSRF** — the app is stateless (cookie-based sessions, not server-side sessions), so CSRF protection is unnecessary.
2. **Stateless session policy** — `SessionCreationPolicy.STATELESS` tells Spring not to create HTTP sessions.
3. **Public endpoints** — `/auth/**` (all auth flows) and Swagger UI endpoints are accessible without authentication.
4. **All other endpoints require authentication** — enforced by `.anyRequest().authenticated()`.
5. **Registers the JWT filter** — `JwtAuthenticationFilter` is inserted *before* `UsernamePasswordAuthenticationFilter` so that cookie-based sessions are resolved before Spring's default authentication runs.
6. **Custom error handlers:**
   - `customAuthenticationEntryPoint()` → Returns a JSON 401 response when no valid session is found.
   - `customAccessDeniedHandler()` → Returns a JSON 403 response when the user's role is insufficient.
7. **Beans exposed:**
   - `PasswordEncoder` — BCrypt encoder used by AuthService and AdminSeeder.
   - `AuthenticationManager` — Used by AuthService for credential validation during login.

### Why It's Needed
Without this class, Spring Security would apply its default form-login configuration which would redirect to a login page and use server-side sessions — none of which is appropriate for a REST API.

---

## 2. MasterLoggingFilter.java

**File:** `config/MasterLoggingFilter.java`  
**Type:** `@Component` Servlet Filter (`OncePerRequestFilter`) — `@Order(HIGHEST_PRECEDENCE)`

### Purpose
The **first filter in the chain**. Every HTTP request passes through this filter before reaching security or controllers. It handles request tracing and logging.

### What It Does
1. **Generates a Trace ID** (`correlationId`) and stores it in SLF4J's MDC. This UUID appears in every log line across all layers (controller, service, repository), making it easy to trace a single request through the logs.
2. **Wraps request/response** in Spring's `ContentCachingRequestWrapper` / `ContentCachingResponseWrapper` so the request body can be logged even after it has been consumed by Jackson.
3. **Two logging modes** controlled by `app.logging.verbose` in `application.properties`:
   - `true` (dev mode): Logs separator lines, method + URI, all cookies, all headers, request body, response status + duration.
   - `false` (production mode): Logs only method + URI and response status + duration.
4. **Copies the response body back** (`copyBodyToResponse()`) — without this, the client would receive an empty body.
5. **Cleans up MDC** in the `finally` block to prevent thread pool leaks.

### Why It's Needed
Without centralized request logging, debugging API issues would require adding log statements to every controller method. This filter provides a single, consistent, and configurable logging point.

---

## 3. RedisConfig.java

**File:** `config/RedisConfig.java`  
**Type:** `@Configuration`

### Purpose
Configures the `RedisTemplate` bean used by `RedisService` to read/write session data and OTPs.

### What It Does
1. **Creates an `ObjectMapper` bean** — used for JSON serialization throughout the app.
2. **Creates a `RedisTemplate<String, Object>` bean** with:
   - `StringRedisSerializer` for keys and hash keys (human-readable keys like `session:abc-123`).
   - `GenericJackson2JsonRedisSerializer` for values (stores DTOs as JSON in Redis).

### Why It's Needed
Without explicit serializer configuration, Spring's default `RedisTemplate` uses Java serialization, which produces opaque binary data that cannot be inspected in Redis CLI and is fragile across class changes.

---

## 4. MailConfig.java

**File:** `config/MailConfig.java`  
**Type:** `@Configuration`

### Purpose
Configures the `JavaMailSender` bean used by `EmailService` to send SMTP emails.

### What It Does
1. Reads `spring.mail.host`, `spring.mail.port`, `spring.mail.username`, `spring.mail.password` from `application.properties`.
2. Creates a `JavaMailSenderImpl` with SMTP/TLS settings.
3. Exposes it as a Spring bean so `EmailService` can inject and use it.

### Why It's Needed
Provides the actual SMTP connection details. Without it, `JavaMailSender` would not be available and email sending would fail at startup.

---

## 5. OpenApiConfig.java

**File:** `config/OpenApiConfig.java`  
**Type:** `@Configuration`

### Purpose
Configures Swagger / OpenAPI documentation for the project.

### What It Does
1. Sets the API title to "Bleep LearnHub API" with version "1.0".
2. Adds a global `bearerAuth` security scheme (HTTP Bearer + JWT format) so that the Swagger UI includes an "Authorize" button where developers can paste a JWT token for testing.

### Why It's Needed
Makes all REST endpoints browsable and testable via the Swagger UI at `/swagger-ui.html`.

---

## 6. AdminSeeder.java

**File:** `config/AdminSeeder.java`  
**Type:** `@Component` implementing `CommandLineRunner`

### Purpose
Seeds the initial Super Admin user on application startup.

### What It Does
1. Runs once when the application starts (`CommandLineRunner`).
2. Checks if a user with username `"admin"` already exists.
3. If not, creates a `User` with:
   - Username: `admin`
   - Email: `adminbleepdemo@yopmail.com`
   - Password: `Admin@123` (BCrypt hashed)
   - Role: `SUPER_ADMIN`
   - Status: `ACTIVE`

### Why It's Needed
The platform needs at least one Super Admin to exist before any Vendors or Partners can be created. This seeder ensures there is always a bootstrap admin account.

---

## 7. LayerLoggingAspect.java (DISABLED)

**File:** `config/LayerLoggingAspect.java`  
**Type:** Plain class (intentionally **NOT** a `@Component`)

### Purpose
Originally an AOP aspect that intercepted controller/service/repository calls for logging. **It has been disabled** because Spring AOP's CGLIB proxying caused `JwtAuthenticationFilter` (a servlet filter) to break during Tomcat startup.

### Current State
- The class body is **empty**.
- All request tracing is now done by `MasterLoggingFilter` and inline `@Slf4j` statements.
- Kept in the codebase as documentation of the decision.

---

## 8. RequestTraceFilter.java (SUPERSEDED)

**File:** `config/RequestTraceFilter.java`  
**Type:** Plain class (intentionally **NOT** a `@Component`)

### Purpose
Originally handled MDC correlation ID generation and request logging. **It has been superseded** by `MasterLoggingFilter`, which now handles all of those responsibilities.

### Current State
- The class body is **empty**.
- Kept in the codebase as documentation.

---

# Services (`service/`)

The service package contains the core business logic. Controllers delegate all work to services, keeping the controller layer thin.

---

## 9. AuthService.java

**File:** `service/AuthService.java`  
**Type:** `@Service`

### Purpose
Handles **all authentication and account lifecycle operations**: login, logout, session retrieval, OTP management, and password setup/reset.

### Key Methods

| Method | What It Does |
|---|---|
| `login(request, deviceDetails)` | Authenticates via `AuthenticationManager`, builds a `LoginResponseDto` (user + vendor/partner based on role), generates a session ID, saves the session to Redis (7-day TTL), saves a `UserSession` audit record to PostgreSQL, and updates `lastLoginAt`. Returns a `LoginResult` record containing the session ID and response DTO. |
| `getSession(sessionId)` | Fetches the cached `LoginResponseDto` from Redis. No database hit. |
| `logout(sessionId)` | Deletes the Redis session and marks the `UserSession` DB record as inactive with a logout timestamp. |
| `sendOtp(usernameOrEmail)` | Resolves the user, enforces a 3-per-30-minute rate limit, generates a 6-digit OTP, stores it in Redis under a random token, sends it via email, and returns the OTP token (for the `otp_session` cookie). |
| `setPassword(otpToken, otp, newPassword)` | Validates the OTP from Redis, sets the new BCrypt password, activates the account, and deletes the OTP session. Handles both first-time setup and forgot-password resets. |
| `forgotUsername(email)` | Sends the username to the email if a matching account exists. Always returns silently to prevent email enumeration. |
| `getAllUsers()` | Returns all users mapped to `UserDataDto`. |

### Dependencies
`UserRepository`, `VendorRepository`, `PartnerRepository`, `UserSessionRepository`, `PasswordEncoder`, `AuthenticationManager`, `EmailService`, `RedisService`.

---

## 10. RedisService.java

**File:** `service/RedisService.java`  
**Type:** `@Service`

### Purpose
Abstracts all Redis operations behind clean method signatures. The rest of the app never interacts with `RedisTemplate` directly.

### Key Operations

| Category | Methods | Redis Key Pattern | TTL |
|---|---|---|---|
| **Session** | `saveSessionData()`, `getSessionData()`, `deleteSession()` | `session:{sessionId}` | 7 days |
| **OTP Session** | `saveOtpSession()`, `getOtpSession()`, `deleteOtpSession()` | `otp_session:{token}` | 30 min |
| **OTP Rate Limit** | `getOtpCount()`, `incrementOtpCount()` | `otp_count:{username}` | 30 min |

### What It Stores
- **Session data:** The full `LoginResponseDto` serialized as JSON. This is the same payload returned by `/auth/login` and `/auth/session`.
- **OTP session data:** `OtpSessionData` (username, email, otp) — used during password set/reset flows.
- **OTP count:** An integer counter per user, auto-expiring every 30 minutes. Limits OTP requests to 3 per window.

### Legacy Methods
Contains `saveOtp()`, `validateOtp()`, `deleteOtp()` marked as `@Deprecated(forRemoval = true)`. These use the old `otp:{username}` key pattern and should be removed in a future cleanup.

---

## 11. EmailService.java

**File:** `service/EmailService.java`  
**Type:** `@Service`

### Purpose
Sends transactional emails via SMTP. All methods are `@Async` so they run in a background thread and do not block the HTTP response.

### Methods

| Method | Email Subject | When Sent |
|---|---|---|
| `sendWelcomeEmail(to, username, role)` | "Welcome to Bleep LearnHub!" | When a new Vendor or Partner account is created. |
| `sendOtpEmail(to, otp)` | "Your Bleep LearnHub OTP" | When a user requests an OTP for password setup/reset. |
| `sendForgotUsernameEmail(to, username)` | "Your Bleep LearnHub Username" | When a user submits the forgot-username form. |

### Error Handling
All methods catch exceptions internally and log the error. They never throw to the caller — email failure should not block the main operation.

---

## 12. VendorService.java

**File:** `service/VendorService.java`  
**Type:** `@Service`

### Purpose
Business logic for managing Vendor entities. Used by both `VendorController` (Super Admin CRUD) and internally for vendor-initiated partner creation.

### Key Methods

| Method | Access | What It Does |
|---|---|---|
| `createVendor(dto)` | Super Admin | Creates a `User` (VENDOR role, PENDING_SETUP) + `Vendor` profile. Sends a welcome email. |
| `getAllVendors()` | Super Admin | Returns all vendors mapped to `VendorProfileResponseDto`. |
| `getVendorById(id)` | Super Admin | Returns a single vendor profile. |
| `updateVendor(id, dto)` | Super Admin | Updates company name, phone, description. If `isActive` changes to `false`, blocks the user and invalidates all sessions. If changed to `true`, reactivates the user. |
| `deleteVendor(id)` | Super Admin | **Cascade deletes:** all partner users, partner profiles, audit log references, and user sessions under this vendor, then the vendor itself. |
| `createPartner(vendorUsername, dto)` | Vendor | Creates a partner under the calling vendor. |
| `getAllPartnersForVendor(vendorUsername)` | Vendor | Returns all partners belonging to the calling vendor. |

---

## 13. PartnerService.java

**File:** `service/PartnerService.java`  
**Type:** `@Service`

### Purpose
Business logic for managing Partner entities. Enforces ownership checks — a Vendor can only manage its own Partners.

### Key Methods

| Method | Access | What It Does |
|---|---|---|
| `createPartner(dto, callerUsername, isSuperAdmin)` | Super Admin / Vendor | Creates a `User` (PARTNER role) + `Partner` profile. Super Admin must supply `vendorId`; Vendors are auto-resolved. |
| `getAllPartners(callerUsername, isSuperAdmin)` | Super Admin / Vendor | Super Admin sees all; Vendor sees only their own partners. |
| `getPartnersByVendorId(vendorId, ...)` | Super Admin / Vendor | Filtered partner list. Vendor can only query their own vendor ID. |
| `getPartnerById(id, ...)` | Super Admin / Vendor | Single partner profile with ownership validation. |
| `updatePartner(id, dto, ...)` | Super Admin / Vendor | Updates profile fields. Handles active/blocked status transitions. |
| `deletePartner(id, ...)` | Super Admin / Vendor | Nullifies audit log references, deletes sessions, partner, and user. |

---

## 14. AdminService.java

**File:** `service/AdminService.java`  
**Type:** `@Service`

### Purpose
Legacy service for Super Admin operations. Provides a simpler `createVendor` and `changeVendorStatus` method.

### Key Methods

| Method | What It Does |
|---|---|
| `createVendor(username, email, companyName)` | Creates a vendor user + profile with a welcome email. Simpler API than `VendorService.createVendor`. |
| `changeVendorStatus(vendorId, statusStr)` | Changes a vendor's `AccountStatus`. If set to `BLOCKED`, invalidates all their active sessions. |

### Note
This service overlaps with `VendorService`. It may be refactored or removed in the future as `VendorService` already covers these operations with richer DTOs.

---

# Security (`security/`)

The security package contains the authentication filter, JWT utilities, the Spring Security `UserDetails` adapter, and cookie management.

---

## 15. JwtAuthenticationFilter.java

**File:** `security/JwtAuthenticationFilter.java`  
**Type:** `@Component` extending `OncePerRequestFilter`

### Purpose
The **core authentication filter**. Despite its name, it now works with **cookie-based sessions** rather than JWT tokens in headers. (The name is historical.)

### How It Works (Per Request)
1. **Extracts the `session_id` cookie** from the request.
2. If no cookie → passes the request through unauthenticated (Spring Security will reject it for protected routes).
3. **Queries Redis** for the session data (`RedisService.getSessionData(sessionId)`).
4. If Redis returns null → session expired or user logged out → passes through unauthenticated.
5. If Redis returns valid `LoginResponseDto` with a `User` object → **populates the Spring Security context** with a `UsernamePasswordAuthenticationToken` containing the username and role as a `SimpleGrantedAuthority`.
6. Downstream controllers and `@PreAuthorize` annotations can now read `Authentication.getName()` (username) and check roles.

### Detailed Logging
Includes extensive step-by-step logging (cookies found, Redis results, security context population) for debugging authentication issues.

### Why Not JWT?
The app evolved from JWT-in-header to cookie-based sessions stored in Redis. The filter class was kept and repurposed rather than renamed.

---

## 16. JwtService.java

**File:** `security/JwtService.java`  
**Type:** `@Service`

### Purpose
Provides JWT token generation and parsing utilities using the `io.jsonwebtoken` (JJWT) library.

### Key Methods

| Method | What It Does |
|---|---|
| `generateToken(username, role)` | Creates a signed JWT with `sub` = username, `role` claim, a random `jti` (JWT ID), and configurable expiration. |
| `extractUsername(token)` | Parses the token and returns the `sub` claim. |
| `extractSessionId(token)` | Parses the token and returns the `jti` claim. |
| `isTokenValid(token, username)` | Checks if the token's subject matches the expected username and is not expired. |

### Configuration
- `application.security.jwt.secret-key` — Base64-encoded HMAC-SHA256 key.
- `application.security.jwt.expiration` — Token lifetime in milliseconds (default: 86400000 = 1 day).

### Current Usage
`JwtService` is **not actively used by `JwtAuthenticationFilter`** (which now uses Redis sessions). However, it remains available for any future token-based needs (e.g., email verification links, API keys).

---

## 17. CustomUserDetailsService.java

**File:** `security/CustomUserDetailsService.java`  
**Type:** `@Service` implementing `UserDetailsService`

### Purpose
Spring Security's bridge to the database for username/password authentication.

### What It Does
- Implements the single required method `loadUserByUsername(String username)`.
- Looks up the `User` entity from `UserRepository`.
- Wraps it in a `UserPrincipal` (the `UserDetails` adapter).
- Throws `UsernameNotFoundException` if no user exists.

### When It's Called
Automatically invoked by Spring Security's `AuthenticationManager` during the `authenticationManager.authenticate()` call in `AuthService.login()`.

---

## 18. UserPrincipal.java

**File:** `security/UserPrincipal.java`  
**Type:** Plain class implementing `UserDetails`

### Purpose
Adapts the `User` JPA entity to Spring Security's `UserDetails` interface. This tells Spring Security how to interpret user data.

### Key Mappings

| UserDetails Method | Implementation |
|---|---|
| `getAuthorities()` | Returns the user's `Role` enum name (e.g. `SUPER_ADMIN`, `VENDOR`) as a `SimpleGrantedAuthority`. |
| `getPassword()` | Returns `user.getPasswordHash()`. |
| `getUsername()` | Returns `user.getUsername()`. |
| `isAccountNonLocked()` | Returns `false` if user status is `BLOCKED` (prevents login). |
| `isEnabled()` | Returns `true` only if status is `ACTIVE` (blocks `PENDING_SETUP` users from logging in before they set a password). |
| `isAccountNonExpired()` | Always `true`. |
| `isCredentialsNonExpired()` | Always `true`. |

### Extra
Exposes a `getId()` method returning the user's UUID as a string.

---

## 19. CookieService.java

**File:** `security/CookieService.java`  
**Type:** `@Service`

### Purpose
Centralizes the creation and clearing of `ResponseCookie` objects. Used by `AuthController` for setting and removing the `session_id` and `otp_session` cookies.

### Methods

| Method | What It Does |
|---|---|
| `createCookie(name, value, maxAgeSeconds)` | Builds an `HttpOnly`, `SameSite=Lax`, `Path=/` cookie. `Secure` is set to `false` for local development (should be `true` in production with HTTPS). |
| `clearCookie(name)` | Builds the same cookie with an empty value and `maxAge=0`, which instructs the browser to delete it. |

### Why It's Needed
Duplicating cookie-building logic across controller methods would be error-prone (e.g., forgetting `HttpOnly` or `SameSite`). This service ensures every cookie follows the same security settings.

---

# How It All Fits Together

```
              ┌─────────────────────────────────┐
              │        HTTP Request              │
              └────────────┬────────────────────┘
                           │
              ┌────────────▼────────────────────┐
              │   MasterLoggingFilter            │
              │   (TraceID, logging, body cache) │
              └────────────┬────────────────────┘
                           │
              ┌────────────▼────────────────────┐
              │   JwtAuthenticationFilter        │
              │   (Cookie → Redis → Auth)        │
              └────────────┬────────────────────┘
                           │
              ┌────────────▼────────────────────┐
              │   SecurityConfig (rules)         │
              │   /auth/** → permitAll           │
              │   everything else → authenticated│
              └────────────┬────────────────────┘
                           │
              ┌────────────▼────────────────────┐
              │       Controller                 │
              │       (thin layer)               │
              └────────────┬────────────────────┘
                           │
              ┌────────────▼────────────────────┐
              │         Service                  │
              │   (AuthService, VendorService,   │
              │    PartnerService, etc.)          │
              └────┬───────┬────────┬───────────┘
                   │       │        │
          ┌────────▼──┐ ┌──▼─────┐ ┌▼──────────┐
          │ Repository│ │ Redis  │ │   Email    │
          │ (JPA/DB)  │ │Service │ │  Service   │
          └───────────┘ └────────┘ └────────────┘
```
