# Bleep LearnHub Backend — Full Project Study

## 1. PROJECT OVERVIEW

**Name:** bleep-learnhub-backend  
**Group:** com.bleep  
**Version:** 0.0.1-SNAPSHOT  
**Description:** A Learning Management System (LMS) backend for Bleep LearnHub — a platform where Vendors create courses/batches/sessions, Partners (training institutes) enroll students, and students access learning content.  
**Tech Stack:** Java 21, Spring Boot 4.1.0, PostgreSQL, Redis, Docker, JWT (JJWT 0.12.5), Spring Security, Spring Data JPA, Spring Mail, SpringDoc OpenAPI (Swagger UI 2.8.5)  
**Build Tool:** Maven  
**Base Path:** `/bleep-learnhub-backend/api/v1`

---

## 2. PROJECT STRUCTURE

```
Bleep-LearnHub-Backend/
├── pom.xml                          # Maven build config
├── Dockerfile                       # Multi-stage Docker build (maven:3.9-eclipse-temurin-21-alpine → eclipse-temurin:21-jre-alpine)
├── docker-compose.yml               # Docker Compose with env-var templating
├── dockerStorageConfigs/
│   ├── docker-compose.yml           # Local dev docker-compose
│   └── volume-data/postgres/        # PostgreSQL persistent volume data
├── logs/
│   └── application.log              # Rolling file logs (when verbose=true)
└── src/main/
    ├── java/com/bleep/learnhub/
    │   ├── BleepLearnhubBackendApplication.java   # Main entry point (@EnableAsync, @EnableScheduling)
    │   ├── config/                   # Security, Redis, Filters, Seeder
    │   ├── constants/                # CookieConstants
    │   ├── controller/               # REST controllers (15 total)
    │   ├── dto/                      # Request/Response DTOs
    │   │   ├── request/              # Incoming request DTOs
    │   │   └── response/             # Outgoing response DTOs
    │   ├── entity/                   # JPA entities (14 total)
    │   │   └── enums/                # Enum types (8 total)
    │   ├── exception/                # GlobalExceptionHandler, BusinessException, ResourceNotFoundException
    │   ├── repository/               # Spring Data JPA repositories (17 total)
    │   ├── security/                 # JWT, Cookie, UserDetails
    │   ├── service/                  # Business logic services (16 total)
    │   │   └── impl/                 # Service implementations (DashboardServiceImpl)
    │   └── sse/                      # SSE (Server-Sent Events) for real-time notifications
    └── resources/
        ├── application.properties   # App config (DB, Redis, Mail, JWT, Rate Limits, Admin)
        └── logback-spring.xml       # Logging config (console + rolling file)
```

---

## 3. ENTITY MODEL (14 Entities)

### 3.1 Core User & Role Entities

| Entity | Table | Key Fields | Relationships |
|--------|-------|------------|---------------|
| **User** | `users` | id (UUID PK), username (unique), email (unique), passwordHash, role (SUPER_ADMIN/VENDOR/PARTNER), status (PENDING_SETUP/ACTIVE/BLOCKED), emailVerified, lastLoginAt, createdBy (self-ref FK), createdAt, updatedAt | 1:1 → Vendor, 1:1 → Partner, 1:N → UserSession, 1:N → AuditLog |
| **Vendor** | `vendors` | id (UUID PK), user_id (FK→users), email, companyName, phone, description, isActive, createdAt | 1:1 → User, 1:N → Partner, 1:N → Notification |
| **Partner** | `partners` | id (UUID PK), user_id (FK→users), vendor_id (FK→vendors), email, companyName, phone, description, isActive, createdAt | 1:1 → User, N:1 → Vendor, 1:N → NotificationRecipient |

### 3.2 Course & Content Entities

| Entity | Table | Key Fields | Relationships |
|--------|-------|------------|---------------|
| **Course** | `courses` | id (UUID PK), title, subtitle, description, category, createdAt, updatedAt | 1:N → Batch |
| **Batch** | `batches` | id (UUID PK), course_id (FK), title, subtitle, description, startingDate, endingDate, createdAt, updatedAt | N:1 → Course, 1:N → Session |
| **Session** | `sessions` | id (UUID PK), course_id, batch_id, sessionType (CLASS/NOTE/ASSIGNMENT/PROJECT), title, subtitle, description, liveLink, recordedLink, resourceLink, sequenceOrder, scheduledDate, scheduledTime, createdAt, updatedAt | N:1 → Course, N:1 → Batch |

### 3.3 Student & Enrollment Entities

| Entity | Table | Key Fields | Relationships |
|--------|-------|------------|---------------|
| **Student** | `students` | id (UUID PK), partner_id, firstName, lastName, fullName (computed), email (unique), phoneNumber, college, branch, academicYear, isDeletedByPartner, createdAt, updatedAt | N:1 → Partner (by partner_id FK) |
| **StudentEnrollment** | `student_enrollments` | id (UUID PK), student_id, course_id, batch_id, courseName, batchName, status (ENROLLED/REMOVED/DELETED), isDeletedByPartner, enrolledAt, completedAt | N:1 → Student, N:1 → Course, N:1 → Batch |
| **StudentSessionLog** | `student_session_logs` | id (UUID PK), student_id, session_id, sessionType, course_id, courseName, batch_id, batchName, entryTime, completionTime, timeSpentSec | N:1 → Student, N:1 → Session |

### 3.4 Access & Complaint Entities

| Entity | Table | Key Fields | Relationships |
|--------|-------|------------|---------------|
| **PartnerAccessRequest** | `partner_access_requests` | id (UUID PK), partner_id, vendor_id, course_id, batch_id, partnerName, courseName, batchName, status (PENDING/APPROVED/REJECTED), requestNote, hasBatchAccess, responseNote, maxStudents, requestedAt, resolvedAt | N:1 → Partner, N:1 → Vendor |
| **StudentComplaint** | `student_complaints` | id (UUID PK), student_id, partner_id, vendor_id, course_id, batch_id, studentName, email, phoneNumber, college, branch, academicYear, courseName, batchName, partnerName, vendorName, complaintTitle, complaintText, status (PENDING/IN_PROGRESS/RESOLVED), vendorRemark, partnerRemark, isResolvedByVendor, isResolvedByPartner, createdAt, updatedAt | N:1 → Student, N:1 → Partner, N:1 → Vendor |

### 3.5 Notification Entities

| Entity | Table | Key Fields | Relationships |
|--------|-------|------------|---------------|
| **Notification** | `notifications` | id (UUID PK), vendor_id (FK→vendors), title, message, targetType (ALL_PARTNERS/COURSE/BATCH/SPECIFIC_PARTNERS), targetEntityId, priority (LOW/NORMAL/HIGH/URGENT), createdAt, updatedAt | N:1 → Vendor, 1:N → NotificationRecipient |
| **NotificationRecipient** | `notification_recipients` | id (UUID PK), notification_id (FK), partner_id (FK), isRead, readAt, delivered, deliveredAt, createdAt | N:1 → Notification, N:1 → Partner |

### 3.6 Audit & Logging Entities

| Entity | Table | Key Fields | Relationships |
|--------|-------|------------|---------------|
| **AuditLog** | `audit_logs` | id (UUID PK), user_id (FK→users, nullable), action, entityName, entityId, ipAddress, payload (JSON), createdAt | N:1 → User |
| **ApiLog** | `api_logs` | id (UUID PK), username, role, sessionId, ipAddress, url, method, requestBody, statusCode, isError, responseBody, errorMessage, successMessage, executionTimeMs, createdAt | None |
| **UserSession** | `user_sessions` | id (UUID PK), user_id (FK→users), sessionId (unique), ipAddress, browser, os, deviceType, device, deviceModel, osVersion, clientVersion, loginAt, logoutAt, isActive | N:1 → User |

### 3.7 Enums

| Enum | Values |
|------|--------|
| **Role** | SUPER_ADMIN, VENDOR, PARTNER |
| **AccountStatus** | PENDING_SETUP, ACTIVE, BLOCKED |
| **SessionType** | CLASS, NOTE, ASSIGNMENT, PROJECT |
| **EnrollmentStatus** | ENROLLED, REMOVED, DELETED |
| **AccessRequestStatus** | PENDING, APPROVED, REJECTED |
| **ComplaintStatus** | PENDING, IN_PROGRESS, RESOLVED |
| **NotificationPriority** | LOW, NORMAL, HIGH, URGENT |
| **NotificationTargetType** | ALL_PARTNERS, COURSE, BATCH, SPECIFIC_PARTNERS |

---

## 4. API ENDPOINTS (Complete List)

### 4.1 Authentication — `/auth/**` (Public)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/login` | Public | Login with username+password, returns session cookie |
| GET | `/auth/session` | Public | Validate session cookie, return profile from Redis |
| POST | `/auth/logout` | Public | Invalidate session, clear cookie |
| POST | `/auth/send-otp` | Public | Send OTP to email, set otp_session cookie |
| POST | `/auth/set-password` | Public | Verify OTP, set new password (setup or reset) |
| POST | `/auth/forgot-username` | Public | Email username to registered address |
| GET | `/auth/users` | SUPER_ADMIN | List all users |

### 4.2 Courses — `/courses/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/courses` | VENDOR | Create a course |
| PUT | `/courses/{id}` | VENDOR | Update a course |
| DELETE | `/courses/{id}` | VENDOR | Delete a course |
| GET | `/courses` | VENDOR, PARTNER | List all courses |
| GET | `/courses/{id}` | VENDOR, PARTNER | Get course by ID |

### 4.3 Batches — `/batches/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/batches` | VENDOR | Create a batch |
| PUT | `/batches/{id}` | VENDOR | Update a batch |
| DELETE | `/batches/{id}` | VENDOR | Delete a batch |
| GET | `/batches` | VENDOR, PARTNER | List all batches (optional ?courseId filter) |
| GET | `/batches/{id}` | VENDOR, PARTNER | Get batch by ID |

### 4.4 Sessions — `/sessions/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/sessions` | VENDOR | Create a session |
| PUT | `/sessions/{id}` | VENDOR | Update a session |
| DELETE | `/sessions/{id}` | VENDOR | Delete a session |
| GET | `/sessions` | VENDOR, PARTNER | List sessions (filters: courseId, batchId, type, search, sortOrder) |
| GET | `/sessions/{id}` | VENDOR, PARTNER | Get session by ID |
| PUT | `/sessions/reorder` | VENDOR | Reorder sessions (sequence) |

### 4.5 Vendors — `/vendors/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/vendors` | SUPER_ADMIN | Create vendor (sends welcome email) |
| GET | `/vendors` | SUPER_ADMIN | List all vendors |
| GET | `/vendors/{id}` | SUPER_ADMIN | Get vendor by ID |
| PUT | `/vendors/{id}` | SUPER_ADMIN | Update vendor |
| DELETE | `/vendors/{id}` | SUPER_ADMIN | Delete vendor + all partners |

### 4.6 Partners — `/partners/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/partners` | SUPER_ADMIN, VENDOR | Create partner (VENDOR auto-assigned to their vendor) |
| GET | `/partners` | SUPER_ADMIN, VENDOR | List partners (VENDOR sees only their own) |
| GET | `/partners/vendor/{vendorId}` | SUPER_ADMIN, VENDOR | List partners by vendor ID |
| GET | `/partners/{id}` | SUPER_ADMIN, VENDOR | Get partner by ID |
| PUT | `/partners/{id}` | SUPER_ADMIN, VENDOR | Update partner (activate/deactivate toggles user BLOCKED/ACTIVE) |
| DELETE | `/partners/{id}` | SUPER_ADMIN, VENDOR | Delete partner (checks no active access, nullifies audit refs, deletes sessions) |

### 4.7 Dashboard — `/dashboard/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/dashboard/vendor/{vendorId}` | SUPER_ADMIN, VENDOR | Vendor dashboard (courses, batches, partners, students, pending requests, recent complaints) |
| GET | `/dashboard/partner/{partnerId}` | SUPER_ADMIN, PARTNER | Partner dashboard (vendor details, students, enrollments, recent complaints) |
| GET | `/dashboard/vendor/{vendorId}/profile` | VENDOR | Get own vendor profile |
| PUT | `/dashboard/vendor/{vendorId}/profile` | VENDOR | Update own vendor profile |
| GET | `/dashboard/partner/{partnerId}/profile` | PARTNER | Get own partner profile |
| PUT | `/dashboard/partner/{partnerId}/profile` | PARTNER | Update own partner profile |

### 4.8 Partner Access — `/partner-access/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/partner-access/courses` | PARTNER | Get courses (with access status) — ?partnerId for "My Courses", omit for "Store" |
| GET | `/partner-access/batches` | PARTNER | Get batches for a course (with access status) |
| GET | `/partner-access/sessions` | PARTNER | Get sessions for a batch (links hidden if not approved) |
| GET | `/partner-access/schedule` | PARTNER | Get calendar schedule (fromDate, toDate, partnerId) |
| GET | `/partner-access/students` | PARTNER | Get students (filters: partnerId, batchId, sessionId) |

### 4.9 Access Requests — `/access-requests/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/access-requests` | VENDOR | Vendor creates access request (auto-APPROVED) |
| POST | `/access-requests/request` | PARTNER | Partner requests access (PENDING) |
| PUT | `/access-requests/{id}/status` | VENDOR | Approve/reject access request |
| DELETE | `/access-requests/{id}` | VENDOR | Delete access record |
| GET | `/access-requests/vendor/{vendorId}` | VENDOR, SUPER_ADMIN | Get requests by vendor |
| GET | `/access-requests/partner/{partnerId}` | VENDOR, SUPER_ADMIN | Get requests by partner |

### 4.10 Partner Portal (Vendor View) — `/partner-portal/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/partner-portal/partner/{partnerId}/courses` | VENDOR | Get partner's approved courses |
| GET | `/partner-portal/partner/{partnerId}/courses/{courseId}/batches` | VENDOR | Get partner's batches for a course |
| GET | `/partner-portal/partner/{partnerId}/batches/{batchId}/sessions` | VENDOR | Get sessions with student counts and limit-exceeded flags |
| GET | `/partner-portal/partner/{partnerId}/students` | VENDOR | Get partner's students with enrollments |
| GET | `/partner-portal/partner/{partnerId}/sessions/limit-crossed` | VENDOR | Get sessions where student count exceeds maxStudents |

### 4.11 Complaints — `/complaints/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/complaints` | VENDOR, PARTNER | Create complaint |
| PUT | `/complaints/{id}` | VENDOR, PARTNER | Update complaint (vendor/partner remarks, resolve flags) |
| DELETE | `/complaints/{id}` | VENDOR, PARTNER | Delete complaint |
| GET | `/complaints` | VENDOR, PARTNER | List complaints (?vendorId or ?partnerId required) |
| GET | `/complaints/{id}` | VENDOR, PARTNER | Get complaint by ID |

### 4.12 Vendor Notifications — `/vendors/notifications/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/vendors/notifications` | VENDOR | Create and send notification (resolves target partners, saves recipients, pushes SSE) |
| GET | `/vendors/notifications` | VENDOR | Get vendor's notification history (paginated) |
| PUT | `/vendors/notifications/{id}` | VENDOR | Update notification |
| DELETE | `/vendors/notifications/{id}` | VENDOR | Delete notification + recipients |

### 4.13 Partner Notifications — `/partners/notifications/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/partners/notifications` | PARTNER | Get partner's received notifications (paginated) |
| GET | `/partners/notifications/unread-count` | PARTNER | Get unread notification count |
| PATCH | `/partners/notifications/{id}/read` | PARTNER | Mark single notification as read |
| PATCH | `/partners/notifications/read-all` | PARTNER | Mark all notifications as read |
| GET | `/partners/notifications/stream` | PARTNER | SSE stream for real-time notifications |

### 4.14 Open/Public Sync — `/open/sync/**` (Public)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/open/sync/student-data` | Public | Sync student data (create/update student, enrollment, session log) |
| POST | `/open/sync/complaints` | Public | Create complaint from external system |
| GET | `/open/sync/courses` | Public | Get partner's courses |
| GET | `/open/sync/courses/{courseId}/batches` | Public | Get partner's batches for a course |

### 4.15 Admin Master Data — `/admin/master/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/admin/master/{entity}` | SUPER_ADMIN | Paginated list with search for: batches, courses, partners, partner-access-requests, sessions, students, student-complaints, student-enrollments, users, user-sessions, vendors, student-session-logs |
| DELETE | `/admin/master/{entity}/{id}` | SUPER_ADMIN | Delete single entity |
| DELETE | `/admin/master/{entity}` | SUPER_ADMIN | Delete all entities of type |

### 4.16 Admin Audit Logs — `/admin/audit-logs/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/admin/audit-logs` | SUPER_ADMIN | Paginated audit logs with search |
| DELETE | `/admin/audit-logs/{id}` | SUPER_ADMIN | Delete single audit log |
| DELETE | `/admin/audit-logs` | SUPER_ADMIN | Delete all audit logs |

### 4.17 Admin API Logs — `/admin/logs/**`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/admin/logs` | SUPER_ADMIN | Paginated API logs (newest first, default 100/page) |
| DELETE | `/admin/logs/{id}` | SUPER_ADMIN | Delete single API log |
| DELETE | `/admin/logs` | SUPER_ADMIN | Delete all API logs |

---

## 5. SECURITY ARCHITECTURE

### 5.1 Authentication Flow

1. **Login:** User sends username+password → `AuthService.login()` authenticates via Spring Security `AuthenticationManager` → builds `LoginResponseDto` (User + optional Vendor/Partner data) → saves to Redis with 7-day TTL → saves `UserSession` record in PostgreSQL → returns `session_id` cookie (HttpOnly, SameSite=Lax)
2. **Session Validation:** Every request → `JwtAuthenticationFilter` extracts `session_id` cookie → fetches `LoginResponseDto` from Redis → populates `SecurityContext` with username + role as `SimpleGrantedAuthority`
3. **Logout:** Deletes Redis session → marks `UserSession.isActive=false` → clears cookie

### 5.2 Password Setup/Reset Flow

1. User requests OTP via `/auth/send-otp` (rate-limited: 100 per 30 min per user)
2. 6-digit OTP generated, stored in Redis under random token (30-min TTL), emailed to user
3. `otp_session` cookie set with the token
4. User submits OTP + new password via `/auth/set-password`
5. OTP validated against Redis → password hashed with BCrypt → account activated (PENDING_SETUP → ACTIVE) → OTP session deleted

### 5.3 Authorization

- **SUPER_ADMIN:** Full access — manage vendors, all master data, audit logs, API logs
- **VENDOR:** Manage courses/batches/sessions, manage own partners, create notifications, manage access requests, view partner portal, view own dashboard
- **PARTNER:** View accessible courses/batches/sessions, view schedule, manage students, view notifications (SSE), view own dashboard, create complaints
- **Public:** Auth endpoints, open sync endpoints, Swagger UI

### 5.4 Security Components

| Component | Role |
|-----------|------|
| `SecurityConfig` | Configures filter chain: CSRF disabled, stateless sessions, CORS, public/authenticated URL patterns, custom 401/403 handlers |
| `JwtAuthenticationFilter` | Cookie-based auth: extracts `session_id` cookie, validates against Redis, populates SecurityContext |
| `CookieService` | Creates/clears HttpOnly cookies (session_id: 7 days, otp_session: 30 min) |
| `UserPrincipal` | Implements UserDetails — wraps User entity, checks AccountStatus for enabled/locked |
| `CustomUserDetailsService` | Loads User by username for AuthenticationManager |
| `JwtService` | JWT utility (JJWT 0.12.5) — generate, validate, extract claims (HS256) |
| `ApiLoggingFilter` | Rate limiting (Redis-based, 200 req/min per IP), request/response body logging to `api_logs` table (async) |
| `MasterLoggingFilter` | MDC correlationId generation, verbose/quiet request logging (controlled by `app.logging.verbose`) |

---

## 6. INFRASTRUCTURE & CONFIGURATION

### 6.1 Database: PostgreSQL
- Host: `host.docker.internal` (Docker) or `localhost` (local)
- Port: 5432
- JPA: `ddl-auto=update` (auto-creates/updates schema)
- All entity IDs use UUID (GenerationType.UUID)

### 6.2 Cache: Redis
- Host: `host.docker.internal` (Docker) or `localhost` (local)
- Port: 6380
- Uses: Session storage (7-day TTL), OTP storage (30-min TTL), OTP rate limiting, API rate limiting
- Serialization: String keys, JSON values (GenericJackson2JsonRedisSerializer)

### 6.3 Email: Gmail SMTP
- Host: smtp.gmail.com, Port: 587
- STARTTLS enabled
- Async sending via `@Async` on EmailService methods

### 6.4 JWT Configuration
- Secret key: Base64-encoded 256-bit key
- Expiration: 86400000ms (24 hours)
- Algorithm: HS256

### 6.5 Rate Limiting
- Global API: 200 requests per minute per IP
- OTP: 100 requests per 30 minutes per user

### 6.6 Admin Seeder
- On startup, if no user with configured admin username exists, creates SUPER_ADMIN user
- Configurable via `app.admin.*` properties

### 6.7 Docker
- Multi-stage build: Maven 3.9 + Eclipse Temurin 21 Alpine → JRE 21 Alpine
- Non-root user (`spring:spring`)
- Exposes port 8080
- docker-compose.yml uses env-var templating (`${VAR}` syntax)

### 6.8 Logging
- Logback with conditional rolling file appender (only when `app.logging.verbose=true`)
- Pattern: `timestamp [thread] [TraceID: correlationId] LEVEL logger - message`
- Rolling: daily or 10MB, 30-day retention, 1GB total cap
- Framework noise suppressed (WARN level for Spring, Hibernate, Security, Lettuce, Mail)

---

## 7. REAL-TIME NOTIFICATIONS (SSE)

- **SseConnectionManager:** Manages concurrent SSE connections per partner (ConcurrentHashMap<UUID, SseEmitter>)
- **HeartbeatService:** Sends ping every 30 seconds to keep connections alive, removes dead connections
- **NotificationSender:** Pushes `NotificationEventDto` to connected partners via SSE when vendor creates a notification
- **Flow:** Vendor creates notification → `NotificationService` resolves target partners → saves `NotificationRecipient` records → `NotificationSender` pushes to online partners via SSE → offline partners see notifications on next poll
- SSE stream endpoint: `GET /partners/notifications/stream` (PARTNER only)
- Emitter timeout: 1 hour

---

## 8. KEY BUSINESS LOGIC

### 8.1 Partner Access Model
- Partners request access to courses/batches (PENDING)
- Vendors approve/reject requests
- Access can be course-level (all batches) or batch-level (specific batch)
- `maxStudents` can be set per access request
- `PartnerPortalService` tracks student counts per session and flags `limitExceeded`

### 8.2 Student Data Sync
- External systems can POST to `/open/sync/student-data` with student, enrollment, and session log data
- Idempotent: checks existing records by ID or email before creating
- Creates/updates Student, StudentEnrollment, and StudentSessionLog in one transaction

### 8.3 Complaint Management
- Dual-resolution: both vendor and partner can add remarks and mark as resolved
- `isResolvedByVendor` and `isResolvedByPartner` flags
- Status: PENDING → IN_PROGRESS → RESOLVED

### 8.4 Dashboard Metrics
- **Vendor Dashboard:** totalCourses, totalBatches, totalPartners, totalStudents, pendingAccessRequests, recentComplaints (top 5)
- **Partner Dashboard:** vendorDetails, totalStudents, activeEnrollments, recentComplaints (top 5)

### 8.5 Account Lifecycle
- Created → PENDING_SETUP (cannot login)
- OTP verified + password set → ACTIVE
- Deactivated by admin → BLOCKED (all sessions invalidated)
- Reactivated → ACTIVE

---

## 9. EXCEPTION HANDLING

`GlobalExceptionHandler` (@RestControllerAdvice) handles:

| Exception | HTTP Status | Error Code |
|-----------|-------------|------------|
| MethodArgumentNotValidException | 400 | VALIDATION_ERROR |
| ResourceNotFoundException | 404 | RESOURCE_NOT_FOUND |
| BusinessException | 400 | BUSINESS_RULE_VIOLATION |
| BadCredentialsException | 400 | BAD_CREDENTIALS |
| DisabledException | 403 | ACCOUNT_NOT_ACTIVE |
| LockedException | 403 | ACCOUNT_BLOCKED |
| AccessDeniedException | 403 | ACCESS_DENIED |
| DataIntegrityViolationException | 400 | DATA_INTEGRITY_VIOLATION |
| Exception (fallback) | 500 | INTERNAL_SERVER_ERROR |

---

## 10. API RESPONSE FORMAT

All responses use `ApiResponse<T>`:

```json
{
  "data": { ... },
  "message": "Success message",
  "error": "200",
  "errorCode": null
}
```

Error responses:
```json
{
  "data": null,
  "message": "Error description",
  "error": "400",
  "errorCode": "VALIDATION_ERROR"
}
```

---

## 11. REPOSITORIES (17 Total)

| Repository | Entity | Notable Custom Queries |
|-----------|--------|------------------------|
| UserRepository | User | findByUsername, findByEmail, existsByUsername, existsByEmail |
| VendorRepository | Vendor | findByUserUsername |
| PartnerRepository | Partner | findByUserUsername, findByVendorId, findByVendorUserUsername, countByVendorId |
| CourseRepository | Course | (standard JPA) |
| BatchRepository | Batch | findByCourseId, findByCourseIdIn |
| SessionRepository | Session | findByBatchIdOrderBySequenceOrderAsc, findByBatchIdsAndScheduledDateBetween |
| StudentRepository | Student | findByEmail, findByPartnerId, countByPartnerId, countByPartnerIdIn |
| StudentEnrollmentRepository | StudentEnrollment | findByStudentId, findByStudentIdAndCourseIdAndBatchId, findStudentIdsByBatchIdAndPartnerId |
| StudentSessionLogRepository | StudentSessionLog | findByStudentIdAndSessionId, findStudentIdsBySessionIdAndPartnerId, countDistinctStudentsBySessionIdAndPartnerId |
| PartnerAccessRequestRepository | PartnerAccessRequest | findByPartnerId, findByVendorId, existsByPartnerIdAndCourseIdAndBatchId, countByVendorIdAndStatus, existsByPartnerIdAndStatus |
| StudentComplaintRepository | StudentComplaint | findTop5ByVendorIdOrderByCreatedAtDesc, findTop5ByPartnerIdOrderByCreatedAtDesc |
| NotificationRepository | Notification | findByVendorIdOrderByCreatedAtDesc, findByIdAndVendorId |
| NotificationRecipientRepository | NotificationRecipient | findByPartnerIdOrderByCreatedAtDesc, countByNotificationId, countByNotificationIdAndIsReadTrue, countByPartnerIdAndIsReadFalse, markAsRead, markAllAsReadForPartner, deleteByNotificationId |
| AuditLogRepository | AuditLog | nullifyUserReferences |
| ApiLogRepository | ApiLog | (standard JPA) |
| UserSessionRepository | UserSession | findBySessionId, invalidateAllSessionsForUser, deleteByUserId |

---

## 12. SERVICES (16 Total)

| Service | Responsibility |
|---------|---------------|
| **AuthService** | Login, logout, session validation, OTP send/verify, password set/reset, forgot username, user list |
| **RedisService** | Session CRUD (7-day TTL), OTP session CRUD (30-min TTL), OTP rate limiting, API rate limiting |
| **EmailService** | Async email sending: welcome, OTP, forgot-username |
| **VendorService** | Vendor CRUD, partner creation under vendor, partner listing |
| **PartnerService** | Partner CRUD with access control (SUPER_ADMIN vs VENDOR scoping), activate/deactivate with session invalidation |
| **CourseService** | Course CRUD |
| **BatchService** | Batch CRUD |
| **SessionService** | Session CRUD, reordering |
| **DashboardService** (interface) + **DashboardServiceImpl** | Vendor/partner dashboard metrics |
| **PartnerAccessService** | Partner-facing course/batch/session browsing with access status, calendar schedule, student listing |
| **PartnerPortalService** | Vendor-facing partner data: courses, batches, sessions (with student counts and limit checks), students |
| **PartnerAccessRequestService** | Access request CRUD (vendor-created auto-approved, partner-created pending) |
| **StudentComplaintService** | Complaint CRUD with dual-resolution (vendor + partner remarks) |
| **StudentDataSyncService** | Idempotent student/enrollment/session-log sync from external systems |
| **NotificationService** | Vendor: create/send (with target resolution + SSE push), list, update, delete. Partner: list, unread count, mark read |
| **NotificationSender** | SSE push to connected partners |
| **HeartbeatService** | SSE keep-alive pings every 30s |
| **AuditLogService** | Audit log CRUD |
| **ApiLogService** | API log CRUD |
| **MasterDataService** | Admin bulk CRUD for all entity types |
| **AdminService** | (referenced but minimal) |

---

## 13. GIT INFO

- **Current Branch:** dev → origin/dev
- **Status:** clean
- **Recent Commits:**
  - `4959cad` — "done some changes"
  - `859082f` — "working on it"
  - `e8087e6` — "added notification"

---

## 14. KEY DESIGN PATTERNS & NOTES

1. **Cookie-based sessions** — No JWT in headers; session_id is an HttpOnly cookie pointing to Redis-stored session data
2. **Redis as session store** — 7-day TTL, JSON serialized LoginResponseDto
3. **Data isolation** — Vendors can only see their own partners; Partners can only see their own data; enforced in service layer
4. **Async logging** — ApiLoggingFilter saves API logs via single-thread executor to avoid blocking responses
5. **Idempotent sync** — StudentDataSyncService checks existing records before creating
6. **Dual-resolution complaints** — Both vendor and partner can independently mark complaints as resolved
7. **SSE for real-time** — Notifications pushed to connected partners; heartbeat keeps connections alive
8. **Rate limiting** — Two-tier: global API (200/min/IP) and OTP (100/30min/user), both Redis-backed
9. **Account lifecycle** — PENDING_SETUP → (OTP+password) → ACTIVE → (deactivate) → BLOCKED
10. **Cascade deletion** — Deleting a vendor checks for existing partners first; deleting a partner nullifies audit log references and deletes sessions
