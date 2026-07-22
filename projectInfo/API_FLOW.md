# 🔄 Bleep LearnHub Backend — Complete API Lifecycle Flow

This document traces the **exact path every HTTP request takes** through the codebase,
from the moment it arrives at the server to the moment a response is returned.

---

## 🏁 1. Application Startup

```
BleepLearnhubBackendApplication.java
  └── @SpringBootApplication
      └── SpringApplication.run(...)
          ├── Loads Spring Beans (all @Service, @Repository, @Controller, @Component classes)
          ├── SecurityConfig.java         ──► Builds the SecurityFilterChain
          │     ├── Registers JwtAuthenticationFilter (runs on EVERY request)
          │     ├── Marks /api/v1/auth/** as PUBLIC (no JWT needed)
          │     └── Marks all other endpoints as PROTECTED (JWT required)
          ├── RedisConfig.java            ──► Creates RedisTemplate bean
          ├── MailConfig.java             ──► Creates JavaMailSender bean
          └── OpenApiConfig.java          ──► Sets up Swagger UI
```

---

## 🌐 2. Request Arrives at the Server

Every single HTTP request follows this path:

```
HTTP Request (from client)
  │
  ▼
[Spring Boot Embedded Tomcat]
  │
  ▼
[Spring Security Filter Chain]   ← SecurityConfig.java controls this
  │
  ▼
JwtAuthenticationFilter.java     ← This runs BEFORE any controller
```

---

## 🔐 3. JwtAuthenticationFilter — The Security Gateway

**File:** `src/main/java/com/bleep/learnhub/security/JwtAuthenticationFilter.java`

This filter intercepts EVERY request and decides if it's allowed through.

```
JwtAuthenticationFilter.doFilterInternal(request, response, filterChain)
  │
  ├── STEP 1: Read "Authorization: Bearer <token>" from request header
  │     ├── If MISSING → pass through (Spring Security will reject it later for protected routes)
  │     └── If PRESENT → extract the raw JWT string
  │
  ├── STEP 2: Extract data from JWT
  │     ├── JwtService.extractUsername(jwt)   ──► gets the username claim
  │     └── JwtService.extractSessionId(jwt)  ──► gets the "jti" UUID claim
  │
  ├── STEP 3: Check if session is alive in Redis
  │     └── RedisService.isSessionActive(sessionId)
  │           ├── If FALSE  → response.sendError(401, "Session expired or revoked") ──► STOP
  │           └── If TRUE   → continue
  │
  ├── STEP 4: Load full user details from database
  │     └── UserDetailsService.loadUserByUsername(username)
  │           └── Returns UserPrincipal.java (wraps the User entity from DB)
  │
  ├── STEP 5: Validate JWT signature & expiration
  │     └── JwtService.isTokenValid(jwt, username)
  │           ├── If INVALID → sendError(401) ──► STOP
  │           └── If VALID   → continue
  │
  └── STEP 6: Set Authentication in Spring Security Context
        └── SecurityContextHolder.getContext().setAuthentication(authToken)
              └── Now downstream code knows WHO this user is and their ROLE
                  └── filterChain.doFilter(request, response) ──► pass to Controller
```

---

## 📬 4a. Flow: Public Auth Endpoints (No JWT Required)

These endpoints live under `/api/v1/auth/**` — they skip JWT validation.

---

### 🔑 4a-i. POST /api/v1/auth/send-otp — Send OTP

```
HTTP POST /api/v1/auth/send-otp?username=john
  │
  ▼
[JwtAuthenticationFilter]
  └── No token? → just passes through (public route)
  │
  ▼
AuthController.java  →  sendOtp(@RequestParam String username)
  │
  ▼
AuthService.java  →  generateAndSendOtp(username)
  │
  ├── UserRepository.findByUsername(username)
  │     └── [PostgreSQL] SELECT * FROM users WHERE username = ?
  │           ├── Not found → throw RuntimeException("User not found") ──► GlobalExceptionHandler → 500
  │           └── Found     → User entity
  │
  ├── Generate 6-digit OTP  (SecureRandom)
  │
  ├── RedisService.saveOtp(username, otp)
  │     └── [Redis] SET otp:{username} {otp} EX 300   (5 min TTL)
  │
  └── EmailService.sendOtpEmail(user.getEmail(), otp)
        └── [SMTP] Sends email asynchronously via JavaMailSender (MailConfig.java)
  │
  ▼
AuthController.java  →  ResponseEntity.ok(ApiResponse.success("OTP sent successfully..."))
  │
  ▼
HTTP 200 OK  { "success": true, "message": "OTP sent successfully to registered email." }
```

---

### 🔑 4a-ii. POST /api/v1/auth/set-password — Verify OTP & Set Password

```
HTTP POST /api/v1/auth/set-password
Body: { "username": "john", "otp": "123456", "newPassword": "Secret@123" }
  │
  ▼
AuthController.java  →  setPassword(@RequestBody SetPasswordDto dto)
  │
  ▼
AuthService.java  →  verifyOtpAndSetPassword(username, otp, newPassword)
  │
  ├── RedisService.validateOtp(username, otp)
  │     └── [Redis] GET otp:{username}  →  compare with provided OTP
  │           ├── Invalid/expired → throw RuntimeException("Invalid or expired OTP") → 500
  │           └── Valid           → continue
  │
  ├── UserRepository.findByUsername(username)
  │     └── [PostgreSQL] SELECT * FROM users WHERE username = ?
  │
  ├── user.setPasswordHash(BCryptPasswordEncoder.encode(newPassword))
  ├── user.setStatus(AccountStatus.ACTIVE)
  ├── user.setEmailVerified(true)
  │
  ├── UserRepository.save(user)
  │     └── [PostgreSQL] UPDATE users SET password_hash=?, status=?, email_verified=? WHERE id=?
  │
  └── RedisService.deleteOtp(username)
        └── [Redis] DEL otp:{username}   (can't be reused)
  │
  ▼
AuthController.java  →  ResponseEntity.ok(ApiResponse.success("Password set successfully..."))
  │
  ▼
HTTP 200 OK  { "success": true, "message": "Password set successfully. You can now log in." }
```

---

### 🔑 4a-iii. POST /api/v1/auth/login — Login & Get JWT

```
HTTP POST /api/v1/auth/login
Body: { "username": "john", "password": "Secret@123" }
  │
  ▼
AuthController.java  →  login(@RequestBody LoginRequestDto loginRequest)
  │
  ▼
AuthService.java  →  authenticateAndGenerateToken(loginRequest)
  │
  ├── STEP 1: Authenticate via Spring Security
  │     └── AuthenticationManager.authenticate(UsernamePasswordAuthenticationToken)
  │           ├── Internally calls UserDetailsService.loadUserByUsername(username)
  │           │     └── [PostgreSQL] SELECT * FROM users WHERE username = ?
  │           │           └── Returns UserPrincipal (wraps User entity)
  │           ├── BCryptPasswordEncoder.matches(rawPassword, storedHash)
  │           │     ├── Wrong password → throw BadCredentialsException
  │           │     │     └── GlobalExceptionHandler → HTTP 401 "Invalid username or password"
  │           │     └── Correct        → Authentication object returned
  │           └── UserPrincipal.isEnabled() / isAccountNonLocked() checked
  │                 ├── BLOCKED account   → throw LockedException
  │                 └── PENDING account   → throw DisabledException
  │
  ├── STEP 2: Double-check account status
  │     └── if user.getStatus() != ACTIVE → throw RuntimeException
  │
  ├── STEP 3: Generate JWT Token
  │     └── JwtService.generateToken(username, role)
  │           ├── Builds claims: { sub: username, role: "VENDOR", jti: UUID, iat, exp }
  │           └── Signs with HS256 using secret key from application.properties
  │
  ├── STEP 4: Extract Session ID from token
  │     └── JwtService.extractSessionId(token)  →  gets the "jti" UUID
  │
  └── STEP 5: Save session to Redis
        └── RedisService.saveSession(sessionId, username, 7)
              └── [Redis] SET session:{sessionId} {username} EX 604800  (7 days TTL)
  │
  ▼
AuthController.java  →  ResponseEntity.ok(ApiResponse.success(token, "Login successful."))
  │
  ▼
HTTP 200 OK  { "success": true, "message": "Login successful.", "data": "eyJhbGci..." }
```

---

### 🔑 4a-iv. POST /api/v1/auth/logout — Logout

```
HTTP POST /api/v1/auth/logout
Header: Authorization: Bearer eyJhbGci...
  │
  ▼
AuthController.java  →  logout(@RequestHeader("Authorization") String token)
  │
  ▼
AuthService.java  →  logoutUser(token)
  │
  ├── JwtService.extractSessionId(token)  →  gets "jti" UUID
  │
  └── RedisService.deleteSession(sessionId)
        └── [Redis] DEL session:{sessionId}
              └── Next request with this token will fail at JwtAuthenticationFilter step 3
  │
  ▼
HTTP 200 OK  { "success": true, "message": "Logged out successfully." }
```

---

## 🔒 4b. Flow: Protected Endpoints (JWT Required)

These require a valid JWT. The filter runs FIRST (Section 3 above), then the controller.

---

### 🏪 4b-i. POST /api/v1/vendors/partners — Vendor Creates a Partner

```
HTTP POST /api/v1/vendors/partners
Header: Authorization: Bearer eyJhbGci...
Body: { "username": "partner1", "email": "p1@co.com", "companyName": "Acme", "phone": "..." }
  │
  ▼
[JwtAuthenticationFilter]  ──►  validates token, sets Authentication in context
  │
  ▼
[Spring Security @PreAuthorize("hasAuthority('VENDOR')")]
  └── Checks the authority in SecurityContextHolder
        ├── Not "VENDOR" → throw AccessDeniedException
        │     └── GlobalExceptionHandler → HTTP 403 "You do not have permission..."
        └── Is "VENDOR"  → continue
  │
  ▼
VendorController.java  →  createPartner(@RequestBody PartnerCreateDto dto, Authentication auth)
  │
  ├── authentication.getName()  ──►  gets logged-in vendor's username from Security context
  │
  ▼
VendorService.java  →  createPartner(vendorUsername, dto)
  │
  ├── STEP 1: Check uniqueness
  │     ├── UserRepository.existsByUsername(dto.username)
  │     │     └── [PostgreSQL] SELECT count(*) FROM users WHERE username = ?
  │     └── UserRepository.existsByEmail(dto.email)
  │           └── [PostgreSQL] SELECT count(*) FROM users WHERE email = ?
  │                 └── Either exists → throw RuntimeException("Username or Email already exists")
  │
  ├── STEP 2: Fetch parent Vendor entity
  │     └── VendorRepository.findByUserUsername(vendorUsername)
  │           └── [PostgreSQL] SELECT v.* FROM vendors v JOIN users u ON v.user_id=u.id WHERE u.username=?
  │
  ├── STEP 3: Create User record for the Partner
  │     └── UserRepository.save(User.builder()
  │               .username(dto.username)
  │               .email(dto.email)
  │               .role(Role.PARTNER)
  │               .status(AccountStatus.PENDING_SETUP)  ← forces OTP flow
  │               .createdBy(vendor.getUser())          ← audit trail
  │           )
  │           └── [PostgreSQL] INSERT INTO users (...) VALUES (...)
  │
  ├── STEP 4: Create Partner profile
  │     └── PartnerRepository.save(Partner.builder()
  │               .user(partnerUser)
  │               .vendor(parentVendor)
  │               .companyName(dto.companyName)
  │               .phone(dto.phone)
  │               .isActive(true)
  │           )
  │           └── [PostgreSQL] INSERT INTO partners (...) VALUES (...)
  │
  └── STEP 5: Send welcome email
        └── EmailService.sendWelcomeEmail(email, username, "Partner")
              └── [SMTP via JavaMailSender]  ← async email
  │
  ▼
VendorController.java  →  ResponseEntity.ok(ApiResponse.success("Partner created successfully..."))
  │
  ▼
HTTP 200 OK  { "success": true, "message": "Partner created successfully under your vendor account." }
```

---

### 🏪 4b-ii. GET /api/v1/vendors/partners — Vendor Lists Their Partners

```
HTTP GET /api/v1/vendors/partners
Header: Authorization: Bearer eyJhbGci...
  │
  ▼
[JwtAuthenticationFilter]  ──►  validates token, sets Authentication
  │
  ▼
[Spring Security @PreAuthorize("hasAuthority('VENDOR')")]  ──►  checks role
  │
  ▼
VendorController.java  →  getMyPartners(Authentication authentication)
  │
  ├── authentication.getName()  ──►  vendorUsername from Security context
  │
  ▼
VendorService.java  →  getAllPartnersForVendor(vendorUsername)
  │
  ├── PartnerRepository.findByVendorUserUsername(vendorUsername)
  │     └── [PostgreSQL] SELECT p.* FROM partners p
  │                         JOIN vendors v ON p.vendor_id = v.id
  │                         JOIN users u ON v.user_id = u.id
  │                         WHERE u.username = ?
  │
  └── .stream().map(partner → PartnerProfileResponseDto.builder()...)
        └── Maps each Partner entity → PartnerProfileResponseDto (no DB exposure)
  │
  ▼
VendorController.java  →  ResponseEntity.ok(ApiResponse.success(partners, "Partners list retrieved..."))
  │
  ▼
HTTP 200 OK  { "success": true, "data": [ { "id":..., "username":..., ... } ] }
```

---

## 💥 5. Error Handling — GlobalExceptionHandler

**File:** `src/main/java/com/bleep/learnhub/exception/GlobalExceptionHandler.java`

Any exception thrown ANYWHERE in the flow is caught here and standardized:

```
Exception thrown (anywhere in Controller/Service/Repository)
  │
  ▼
GlobalExceptionHandler.java  (@RestControllerAdvice intercepts it)
  │
  ├── MethodArgumentNotValidException   ──► HTTP 400  { errors: { field: "message" } }
  │     (from @Valid on DTOs)
  │
  ├── ResourceNotFoundException         ──► HTTP 404  { error: "RESOURCE_NOT_FOUND" }
  │     (entity not found in DB)
  │
  ├── BusinessException                 ──► HTTP 400  { error: "BUSINESS_RULE_VIOLATION" }
  │     (business rule violated)
  │
  ├── BadCredentialsException           ──► HTTP 401  { error: "BAD_CREDENTIALS" }
  │     (wrong password at login)
  │
  ├── AccessDeniedException             ──► HTTP 403  { error: "ACCESS_DENIED" }
  │     (wrong role for endpoint)
  │
  └── Exception (catch-all)             ──► HTTP 500  { error: "INTERNAL_SERVER_ERROR" }
        (logs actual error, hides from client)
```

---

## 📦 6. Data / Entity Layers

These are NOT called directly by controllers — services use them:

```
Repository Layer (Spring Data JPA)
  ├── UserRepository.java          ──► [PostgreSQL] users table
  ├── VendorRepository.java        ──► [PostgreSQL] vendors table
  ├── PartnerRepository.java       ──► [PostgreSQL] partners table
  └── UserSessionRepository.java   ──► [PostgreSQL] user_sessions table

Entity Layer (JPA Entities = DB Table mappings)
  ├── User.java          ──► users table       (id, username, email, passwordHash, role, status)
  ├── Vendor.java        ──► vendors table      (id, user_id FK, companyName)
  ├── Partner.java       ──► partners table     (id, user_id FK, vendor_id FK, companyName, phone)
  ├── UserSession.java   ──► user_sessions table (jti, username, ip, browser, active)
  └── AuditLog.java      ──► audit_logs table   (event, userId, ipAddress, payload)

Enums (finite values for DB columns)
  ├── Role.java          ──► SUPER_ADMIN | VENDOR | PARTNER
  └── AccountStatus.java ──► PENDING_SETUP | ACTIVE | BLOCKED
```

---

## 🗺️ 7. Complete Visual Flow (One Page)

```
 CLIENT
   │
   │  HTTP Request
   ▼
┌──────────────────────────────────────────────────────────┐
│              BleepLearnhubBackendApplication.java         │
│                     (Spring Boot App)                     │
└──────────────────────────────────────────────────────────┘
   │
   ▼
┌──────────────────────────────────────────────────────────┐
│        SecurityConfig.java (Security Filter Chain)        │
│  - /api/v1/auth/**  → PUBLIC (no JWT check)              │
│  - everything else  → PROTECTED (JWT required)            │
└──────────────────────────────────────────────────────────┘
   │
   ▼
┌──────────────────────────────────────────────────────────┐
│          JwtAuthenticationFilter.java (runs always)       │
│  1. Read "Authorization: Bearer ..." header               │
│  2. JwtService.extractUsername() + extractSessionId()     │
│  3. RedisService.isSessionActive(sessionId)               │
│  4. UserDetailsService.loadUserByUsername()               │
│       └── UserPrincipal.java (wraps User entity)         │
│  5. JwtService.isTokenValid()                             │
│  6. SecurityContextHolder.setAuthentication()             │
└──────────────────────────────────────────────────────────┘
   │
   ├─── If token invalid/session dead ──► HTTP 401 STOP
   │
   ▼
┌──────────────────────────────────────────────────────────┐
│                    CONTROLLER LAYER                       │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │AuthController│  │VendorController│  │PartnerController│ │
│  │/api/v1/auth │  │/api/v1/vendors│  │/api/v1/partners│  │
│  └──────┬──────┘  └──────┬───────┘  └────────┬───────┘  │
└─────────│────────────────│──────────────────  │──────────┘
          │                │                    │
          ▼                ▼                    ▼
┌──────────────────────────────────────────────────────────┐
│                     SERVICE LAYER                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │ AuthService │  │VendorService│  │  PartnerService  │  │
│  │             │  │             │  │                  │  │
│  │ EmailService│  │ EmailService│  │                  │  │
│  └──────┬──────┘  └──────┬──────┘  └────────┬────────┘  │
└─────────│────────────────│──────────────────────────────┘
          │                │
          ├────────────────┤
          │                │
   ┌──────▼──────┐  ┌──────▼──────────────────────────┐
   │   REDIS      │  │        POSTGRESQL               │
   │  (Sessions & │  │  UserRepository                 │
   │   OTPs)      │  │  VendorRepository               │
   └─────────────┘  │  PartnerRepository               │
                    │  UserSessionRepository            │
                    └─────────────────────────────────┘
          │
          ▼
┌──────────────────────────────────────────────────────────┐
│              GlobalExceptionHandler.java                  │
│  Catches ALL exceptions → standardized JSON error body    │
└──────────────────────────────────────────────────────────┘
          │
          ▼
        CLIENT
  HTTP Response (200/400/401/403/404/500)
  Body: ApiResponse<T> { success, message, data, error, errorCode }
```

---

## 📋 8. File-to-File Quick Reference

| From File | Calls | To File |
|-----------|-------|---------|
| `BleepLearnhubBackendApplication.java` | starts | Everything via Spring Boot |
| `SecurityConfig.java` | registers | `JwtAuthenticationFilter.java` |
| `JwtAuthenticationFilter.java` | calls | `JwtService.java` (token parse) |
| `JwtAuthenticationFilter.java` | calls | `RedisService.java` (session check) |
| `JwtAuthenticationFilter.java` | calls | `UserDetailsService` → `UserPrincipal.java` |
| `AuthController.java` | delegates to | `AuthService.java` |
| `AuthService.java` | calls | `UserRepository.java` (DB lookup) |
| `AuthService.java` | calls | `JwtService.java` (generate/parse token) |
| `AuthService.java` | calls | `RedisService.java` (save/delete OTP & session) |
| `AuthService.java` | calls | `EmailService.java` (send OTP email) |
| `VendorController.java` | delegates to | `VendorService.java` |
| `VendorService.java` | calls | `UserRepository.java` |
| `VendorService.java` | calls | `VendorRepository.java` |
| `VendorService.java` | calls | `PartnerRepository.java` |
| `VendorService.java` | calls | `EmailService.java` (welcome email) |
| `UserPrincipal.java` | wraps | `User.java` (entity) |
| `UserRepository.java` | queries | PostgreSQL `users` table |
| `VendorRepository.java` | queries | PostgreSQL `vendors` table |
| `PartnerRepository.java` | queries | PostgreSQL `partners` table |
| Any Service/Controller | throws → caught by | `GlobalExceptionHandler.java` |
