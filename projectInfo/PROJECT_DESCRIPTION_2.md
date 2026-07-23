# Bleep LearnHub Backend — Complete Project Reference

---

## Table of Contents

1. [Project Summary](#1-project-summary)
2. [Technology Stack](#2-technology-stack)
3. [Project File Structure](#3-project-file-structure)
4. [Enums](#4-enums)
5. [Entities (Database Tables)](#5-entities-database-tables)
6. [DTOs — Request Bodies](#6-dtos--request-bodies)
7. [DTOs — Response Bodies](#7-dtos--response-bodies)
8. [Repositories](#8-repositories)
9. [Security Layer](#9-security-layer)
10. [Configuration Files](#10-configuration-files)
11. [Exception Handling](#11-exception-handling)
12. [Constants](#12-constants)
13. [Services](#13-services)
14. [Controllers & API Reference](#14-controllers--api-reference)
15. [Cross-Cutting Concerns](#15-cross-cutting-concerns)
16. [Redis Usage](#16-redis-usage)
17. [Email System](#17-email-system)
18. [Data Isolation & Permission Model](#18-data-isolation--permission-model)

---

## 1. Project Summary

**Bleep LearnHub Backend** is a multi-tenant SaaS LMS (Learning Management System) backend API built with Spring Boot. The platform connects three types of actors:

| Role | Description |
|------|-------------|
| **SUPER_ADMIN** | The platform operator. Creates and manages all Vendor accounts. Has read access to all data. |
| **VENDOR** | An educational company. Creates Partner accounts, manages Courses, Batches, Sessions, and controls which Partners have access to which content. |
| **PARTNER** | A sub-company/agent under a Vendor. Enrolls students, tracks session attendance, manages their own dashboard, and submits/tracks complaints. |

**Core capabilities:**
- Cookie-based authentication using JWT + Redis session store
- OTP-based password setup and reset via email
- Vendor → Partner → Student hierarchy management
- Course → Batch → Session content management
- Partner access-request system (Vendors approve/reject Partners' requests for course access)
- Real-time student data sync from external systems (open API)
- Student complaints tracking with role-based remarks
- Complete API logging with rate limiting
- Full audit trail for sensitive operations

---

## 2. Technology Stack

| Technology | Purpose |
|------------|---------|
| Java 17 / Spring Boot 3 | Core framework |
| Spring Security | Authentication & authorization |
| Spring Data JPA / Hibernate | ORM & database access |
| PostgreSQL | Primary relational database |
| Redis | Session store, OTP store, rate limiting |
| JWT (jjwt library) | Token generation & signing (stored in Redis, validated via cookie) |
| JavaMailSender / SMTP | Email delivery (welcome, OTP, forgot-username) |
| Lombok | Boilerplate reduction |
| SpringDoc OpenAPI / Swagger UI | Interactive API documentation |
| BCryptPasswordEncoder | Password hashing |

---

## 3. Project File Structure

```
src/main/java/com/bleep/learnhub/
├── BleepLearnhubBackendApplication.java       <- Main entry point
├── config/
│   ├── AdminSeeder.java                       <- Seeds SUPER_ADMIN on startup
│   ├── ApiLoggingFilter.java                  <- HTTP request/response logger + rate limiter
│   ├── LayerLoggingAspect.java                <- DISABLED (intentionally empty)
│   ├── MailConfig.java                        <- Email bean config
│   ├── MasterLoggingFilter.java               <- MDC trace ID injector
│   ├── OpenApiConfig.java                     <- Swagger UI config with JWT auth
│   ├── RedisConfig.java                       <- Redis template bean config
│   ├── RequestTraceFilter.java                <- Injects X-Request-Id into MDC
│   └── SecurityConfig.java                    <- Spring Security filter chain (currently commented)
├── constants/
│   └── CookieConstants.java                   <- Cookie name + TTL constants
├── controller/
│   ├── ApiLogController.java                  <- [SUPER_ADMIN] CRUD on api_logs
│   ├── AuditLogController.java                <- [SUPER_ADMIN] CRUD on audit_logs
│   ├── AuthController.java                    <- Public auth endpoints
│   ├── BatchController.java                   <- [VENDOR/PARTNER] Batch CRUD
│   ├── CourseController.java                  <- [VENDOR/PARTNER] Course CRUD
│   ├── DashboardController.java               <- Dashboard data for VENDOR + PARTNER
│   ├── MasterDataController.java              <- [SUPER_ADMIN] Raw table read + delete
│   ├── OpenDataSyncController.java            <- Open (no auth) sync endpoints for mobile/apps
│   ├── PartnerAccessController.java           <- Partner views approved content
│   ├── PartnerAccessRequestController.java    <- Access request create/approve/reject
│   ├── PartnerController.java                 <- [VENDOR] Partner CRUD
│   ├── PartnerPortalController.java           <- [VENDOR] View partner's view of content
│   ├── SessionController.java                 <- [VENDOR/PARTNER] Session CRUD + reorder
│   ├── StudentComplaintController.java        <- Complaint CRUD
│   └── VendorController.java                  <- [SUPER_ADMIN] Vendor CRUD
├── dto/
│   ├── OtpSessionData.java                    <- Redis OTP payload (not sent to client)
│   ├── StudentDataSyncRequest.java            <- External sync request body
│   ├── request/                               <- All incoming request DTOs (18+ files)
│   └── response/                              <- All outgoing response DTOs (22+ files)
├── entity/
│   ├── ApiLog.java                            <- Table: api_logs
│   ├── AuditLog.java                          <- Table: audit_logs
│   ├── Batch.java                             <- Table: batches
│   ├── Course.java                            <- Table: courses
│   ├── Partner.java                           <- Table: partners
│   ├── PartnerAccessRequest.java              <- Table: partner_access_requests
│   ├── Session.java                           <- Table: sessions
│   ├── Student.java                           <- Table: students
│   ├── StudentComplaint.java                  <- Table: student_complaints
│   ├── StudentEnrollment.java                 <- Table: student_enrollments
│   ├── StudentSessionLog.java                 <- Table: student_session_logs
│   ├── User.java                              <- Table: users
│   ├── UserSession.java                       <- Table: user_sessions
│   ├── Vendor.java                            <- Table: vendors
│   └── enums/
│       ├── AccessRequestStatus.java           <- PENDING, APPROVED, REJECTED
│       ├── AccountStatus.java                 <- PENDING_SETUP, ACTIVE, BLOCKED
│       ├── ComplaintStatus.java               <- PENDING, IN_PROGRESS, RESOLVED
│       ├── EnrollmentStatus.java              <- ENROLLED, REMOVED, DELETED
│       ├── Role.java                          <- SUPER_ADMIN, VENDOR, PARTNER
│       └── SessionType.java                   <- CLASS, NOTE, ASSIGNMENT, PROJECT
├── exception/
│   ├── BusinessException.java                 <- 400 - Business rule violation
│   ├── GlobalExceptionHandler.java            <- Centralized @RestControllerAdvice
│   └── ResourceNotFoundException.java         <- 404 - Entity not found
├── repository/                                <- 14 Spring Data JPA interfaces
├── security/
│   ├── CookieService.java                     <- Creates / clears HttpOnly cookies
│   ├── CustomUserDetailsService.java          <- Loads User by username for Spring Security
│   ├── JwtAuthenticationFilter.java           <- Reads session_id cookie -> Redis -> SecurityContext
│   ├── JwtService.java                        <- JWT generation, validation, claims extraction
│   └── UserPrincipal.java                     <- Wraps User entity for Spring Security
└── service/
    ├── AuthService.java                       <- Login, OTP, set-password, logout
    ├── BatchService.java
    ├── CourseService.java
    ├── DashboardService.java                  <- Interface
    ├── EmailService.java                      <- Async email delivery
    ├── MasterDataService.java                 <- Admin-level raw table operations
    ├── PartnerAccessRequestService.java       <- Access request workflow
    ├── PartnerAccessService.java              <- Partner's view of approved content
    ├── PartnerPortalService.java              <- Vendor's view of what Partner can see
    ├── PartnerService.java
    ├── RedisService.java                      <- All Redis interactions
    ├── SessionService.java
    ├── StudentComplaintService.java
    ├── StudentDataSyncService.java            <- Upsert students from external app
    ├── VendorService.java
    └── impl/
        └── DashboardServiceImpl.java          <- Vendor + Partner dashboard logic
```

---

## 4. Enums

### Role
Defines user access levels across the platform.
- `SUPER_ADMIN` — Platform operator (highest privilege)
- `VENDOR` — Educational company owner
- `PARTNER` — Sub-company/agent under a Vendor

### AccountStatus
Controls whether a user can authenticate.
- `PENDING_SETUP` — Account created, but password not yet set via OTP
- `ACTIVE` — Fully operational account
- `BLOCKED` — Account suspended; cannot log in

### AccessRequestStatus
Lifecycle of a partner's request to access a course/batch.
- `PENDING` — Request submitted, awaiting vendor review
- `APPROVED` — Vendor approved access
- `REJECTED` — Vendor denied access

### ComplaintStatus
Lifecycle of a student complaint.
- `PENDING` — Just submitted
- `IN_PROGRESS` — Under investigation
- `RESOLVED` — Resolved by vendor or partner

### EnrollmentStatus
State of a student's course enrollment record.
- `ENROLLED` — Currently enrolled
- `REMOVED` — Enrollment revoked by partner
- `DELETED` — Soft-deleted

### SessionType
Types of learning sessions within a batch.
- `CLASS` — Live or recorded class
- `NOTE` — Study material/notes
- `ASSIGNMENT` — Homework or assignment
- `PROJECT` — Project task

---

## 5. Entities (Database Tables)

### User — Table: `users`
The root authentication entity. All Vendors, Partners, and Admins are Users.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK, auto-generated | Unique identifier |
| username | VARCHAR(100) | UNIQUE, NOT NULL | Login username |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Email address |
| password_hash | TEXT | nullable | BCrypt hashed password |
| role | ENUM(Role) | NOT NULL | SUPER_ADMIN / VENDOR / PARTNER |
| status | ENUM(AccountStatus) | NOT NULL | PENDING_SETUP / ACTIVE / BLOCKED |
| email_verified | BOOLEAN | NOT NULL, default=false | Whether OTP has been used |
| last_login_at | TIMESTAMP | nullable | Set on every successful login |
| created_by | UUID -> users.id | FK self-referencing, nullable | Who created this account |
| created_at | TIMESTAMP | auto-set | Record creation timestamp |
| updated_at | TIMESTAMP | auto-updated | Last update timestamp |

---

### Vendor — Table: `vendors`
Profile of an educational company using the platform.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| user_id | UUID -> users.id | FK, UNIQUE, NOT NULL | 1-to-1 with User |
| email | VARCHAR(255) | nullable | Optional business email |
| company_name | VARCHAR(255) | NOT NULL | Company/brand name |
| phone | VARCHAR(20) | nullable | Contact phone |
| description | TEXT | nullable | About the vendor |
| is_active | BOOLEAN | NOT NULL, default=true | Whether vendor is active |
| created_at | TIMESTAMP | auto-set | Creation timestamp |

---

### Partner — Table: `partners`
A sub-entity (coaching institute, campus, agent) under a Vendor.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| user_id | UUID -> users.id | FK, UNIQUE, NOT NULL | 1-to-1 with User |
| vendor_id | UUID -> vendors.id | FK, NOT NULL | Parent vendor |
| email | VARCHAR(255) | nullable | Optional business email |
| company_name | VARCHAR(255) | NOT NULL | Partner company name |
| phone | VARCHAR(20) | nullable | Contact phone |
| description | TEXT | nullable | About the partner |
| is_active | BOOLEAN | NOT NULL, default=true | Whether partner is active |
| created_at | TIMESTAMP | auto-set | Creation timestamp |

---

### Course — Table: `courses`
A curriculum/program offered by a Vendor.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| title | VARCHAR(255) | NOT NULL | Course title |
| subtitle | VARCHAR(255) | nullable | Short tagline |
| description | TEXT | NOT NULL | Full description |
| category | VARCHAR(255) | NOT NULL | Subject category |
| created_at | TIMESTAMP | auto-set | Creation timestamp |
| updated_at | TIMESTAMP | auto-updated | Last update timestamp |

---

### Batch — Table: `batches`
A scheduled run/cohort of a Course.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| course_id | UUID -> courses.id | NOT NULL | Parent course |
| title | VARCHAR(255) | NOT NULL | Batch title |
| subtitle | VARCHAR(255) | nullable | Short tagline |
| description | TEXT | NOT NULL | Description |
| starting_date | DATE | NOT NULL | Batch start date |
| ending_date | DATE | NOT NULL | Batch end date |
| created_at | TIMESTAMP | auto-set | Creation timestamp |
| updated_at | TIMESTAMP | auto-updated | Last update timestamp |

**Indexes:** `idx_batches_course_id` on `course_id`

---

### Session — Table: `sessions`
An individual learning session within a Batch.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| course_id | UUID | NOT NULL | Parent course |
| batch_id | UUID | NOT NULL | Parent batch |
| session_type | ENUM(SessionType) | NOT NULL | CLASS / NOTE / ASSIGNMENT / PROJECT |
| title | VARCHAR(255) | NOT NULL | Session title |
| subtitle | VARCHAR(255) | nullable | Short tagline |
| description | TEXT | NOT NULL | Description |
| live_link | VARCHAR(2083) | nullable | Live class URL |
| recorded_link | VARCHAR(2083) | nullable | Recording URL |
| resource_link | VARCHAR(2083) | nullable | Resource/document URL |
| sequence_order | INTEGER | NOT NULL | Position in batch sequence |
| scheduled_date | DATE | nullable | Planned session date |
| scheduled_time | TIME | nullable | Planned start time |
| created_at | TIMESTAMP | auto-set | Creation timestamp |
| updated_at | TIMESTAMP | auto-updated | Last update timestamp |

**Indexes:** `idx_sessions_course_id`, `idx_sessions_batch_id`

---

### Student — Table: `students`
A learner registered by a Partner. Not a User (students cannot log in to this backend).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| partner_id | UUID | NOT NULL | Partner who registered this student |
| first_name | VARCHAR(100) | NOT NULL | First name |
| last_name | VARCHAR(100) | nullable | Last name |
| full_name | VARCHAR(255) | auto-computed | Concatenated from first+last on persist |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Student email |
| phone_number | VARCHAR(20) | nullable | Phone |
| college | VARCHAR(255) | nullable | College/institution name |
| branch | VARCHAR(100) | nullable | Department/branch |
| academic_year | VARCHAR(50) | nullable | E.g., "2024-25" |
| is_deleted_by_partner | BOOLEAN | NOT NULL, default=false | Soft delete flag |
| created_at | TIMESTAMP | auto-set | Creation timestamp |
| updated_at | TIMESTAMP | auto-updated | Last update timestamp |

Note: `full_name` is automatically computed via `@PrePersist` / `@PreUpdate` lifecycle hooks.

---

### StudentEnrollment — Table: `student_enrollments`
Records which student is enrolled in which course+batch.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| student_id | UUID | NOT NULL | Reference to student |
| course_id | UUID | NOT NULL | Reference to course |
| batch_id | UUID | NOT NULL | Reference to batch |
| course_name | VARCHAR(255) | NOT NULL | Denormalized course name at enrollment time |
| batch_name | VARCHAR(255) | NOT NULL | Denormalized batch name at enrollment time |
| status | ENUM(EnrollmentStatus) | NOT NULL | ENROLLED / REMOVED / DELETED |
| is_deleted_by_partner | BOOLEAN | NOT NULL, default=false | Soft delete flag |
| enrolled_at | TIMESTAMP | auto-set | Enrollment timestamp |
| completed_at | TIMESTAMP | nullable | Completion timestamp |

**Indexes:** `idx_se_student_id`, `idx_se_course_id`, `idx_se_batch_id`

---

### StudentSessionLog — Table: `student_session_logs`
Tracks individual student access to sessions (entry/exit time, time spent).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| student_id | UUID | NOT NULL | Student who accessed the session |
| session_id | UUID | NOT NULL | Which session was accessed |
| session_type | ENUM(SessionType) | NOT NULL | Type of session |
| course_id | UUID | NOT NULL | Course reference |
| course_name | VARCHAR(255) | NOT NULL | Denormalized course name |
| batch_id | UUID | NOT NULL | Batch reference |
| batch_name | VARCHAR(255) | NOT NULL | Denormalized batch name |
| entry_time | TIMESTAMP | NOT NULL | When student started the session |
| completion_time | TIMESTAMP | nullable | When student completed |
| time_spent_sec | INTEGER | nullable | Total time in seconds |

**Indexes:** `idx_ssl_student_id`, `idx_ssl_session_id`, `idx_ssl_course_id`, `idx_ssl_batch_id`

---

### StudentComplaint — Table: `student_complaints`
A complaint submitted by a student (proxied through Partner or Vendor).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| student_id | UUID | nullable | Student who complained |
| partner_id | UUID | NOT NULL | Partner context |
| vendor_id | UUID | NOT NULL | Vendor context |
| course_id | UUID | nullable | Related course |
| batch_id | UUID | nullable | Related batch |
| student_name | VARCHAR(255) | nullable | Denormalized student name |
| email | VARCHAR(255) | nullable | Student email |
| phone_number | VARCHAR(20) | nullable | Student phone |
| college | VARCHAR(255) | nullable | College name |
| branch | VARCHAR(100) | nullable | Branch/dept |
| academic_year | VARCHAR(50) | nullable | Academic year |
| course_name | VARCHAR(255) | nullable | Denormalized course name |
| batch_name | VARCHAR(255) | nullable | Denormalized batch name |
| partner_name | VARCHAR(255) | nullable | Denormalized partner name |
| vendor_name | VARCHAR(255) | nullable | Denormalized vendor name |
| complaint_title | VARCHAR(255) | nullable | Short complaint title |
| complaint_text | TEXT | nullable | Full complaint body |
| status | ENUM(ComplaintStatus) | NOT NULL | PENDING / IN_PROGRESS / RESOLVED |
| vendor_remark | TEXT | nullable | Vendor's response note |
| partner_remark | TEXT | nullable | Partner's response note |
| is_resolved_by_vendor | BOOLEAN | default=false | Vendor marked resolved |
| is_resolved_by_partner | BOOLEAN | default=false | Partner marked resolved |
| created_at | TIMESTAMP | auto-set | Submission timestamp |
| updated_at | TIMESTAMP | auto-updated | Last update timestamp |

**Indexes:** `idx_sc_student_id`, `idx_sc_partner_id`, `idx_sc_vendor_id`, `idx_sc_course_id`, `idx_sc_batch_id`

---

### PartnerAccessRequest — Table: `partner_access_requests`
A request from a Partner to access a specific Course/Batch.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| partner_id | UUID | NOT NULL | Requesting partner |
| vendor_id | UUID | NOT NULL | Vendor who owns the content |
| course_id | UUID | NOT NULL | Requested course |
| batch_id | UUID | nullable | Specific batch (null = all batches in course) |
| partner_name | VARCHAR(255) | NOT NULL | Denormalized partner name |
| course_name | VARCHAR(255) | NOT NULL | Denormalized course name |
| batch_name | VARCHAR(255) | nullable | Denormalized batch name |
| status | ENUM(AccessRequestStatus) | NOT NULL | PENDING / APPROVED / REJECTED |
| request_note | TEXT | nullable | Message from partner |
| has_batch_access | BOOLEAN | nullable | True if specific batch access granted |
| response_note | TEXT | nullable | Vendor's response message |
| max_students | INTEGER | nullable | Student enrollment limit set by vendor |
| requested_at | TIMESTAMP | auto-set | Request creation timestamp |
| resolved_at | TIMESTAMP | nullable | When vendor took action |

**Indexes:** `idx_par_partner_id`, `idx_par_vendor_id`, `idx_par_course_id`, `idx_par_batch_id`

---

### UserSession — Table: `user_sessions`
Audit record for each login session (device fingerprint, active status).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| user_id | UUID -> users.id | FK, NOT NULL | Who logged in |
| session_id | VARCHAR | UNIQUE, NOT NULL | JWT JTI claim (= Redis key) |
| ip_address | VARCHAR(45) | nullable | Client IP (IPv4 or IPv6) |
| browser | VARCHAR | nullable | Browser name |
| os | VARCHAR | nullable | Operating system |
| device_type | VARCHAR | nullable | mobile/desktop/tablet |
| device | VARCHAR | nullable | Device brand |
| device_model | VARCHAR | nullable | Device model |
| os_version | VARCHAR | nullable | OS version |
| client_version | VARCHAR | nullable | App version |
| login_at | TIMESTAMP | auto-set | Login time |
| logout_at | TIMESTAMP | nullable | Logout time |
| is_active | BOOLEAN | NOT NULL, default=true | Whether session is live |

---

### ApiLog — Table: `api_logs`
One record per HTTP request/response, written asynchronously.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| username | VARCHAR(255) | nullable | Authenticated user (or "ANONYMOUS") |
| role | VARCHAR(50) | nullable | User's role |
| session_id | VARCHAR(255) | nullable | Masked session_id cookie (first 8 chars + **MASKED) |
| ip_address | VARCHAR(100) | nullable | Request IP |
| api_url | VARCHAR(255) | NOT NULL | Request URL |
| method | VARCHAR(10) | NOT NULL | HTTP method |
| request_body | TEXT | nullable | Truncated to 5000 chars |
| status_code | INTEGER | NOT NULL | HTTP response status code |
| is_error | BOOLEAN | NOT NULL | True if status >= 400 |
| response_body | TEXT | nullable | Truncated to 5000 chars |
| error_message | TEXT | nullable | Set on error responses |
| success_message | TEXT | nullable | Set on success responses |
| execution_time_ms | BIGINT | nullable | Request processing time in ms |
| created_at | TIMESTAMP | auto-set | Log timestamp |

---

### AuditLog — Table: `audit_logs`
Business-level event logs for sensitive operations.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| user_id | UUID -> users.id | FK, nullable | Who triggered the event |
| action | VARCHAR(100) | NOT NULL | E.g., "PARTNER_ONBOARDED", "VENDOR_BLOCKED" |
| entity_name | VARCHAR(100) | nullable | E.g., "Partner", "Vendor" |
| entity_id | VARCHAR(36) | nullable | UUID of the affected entity |
| ip_address | VARCHAR(45) | nullable | Request IP |
| payload | TEXT | nullable | JSON snapshot of the triggering request |
| created_at | TIMESTAMP | auto-set | Event timestamp |

---

## 6. DTOs — Request Bodies

### LoginRequestDto
Used by `POST /auth/login`
```json
{
  "username": "string (required)",
  "password": "string (required)"
}
```
Also accepts device fingerprint headers: `Device-Type`, `Device`, `Device-Model`, `OS-Name`, `OS-Version`, `Client-Name`, `Client-Version`

---

### SendOtpRequestDto
Used by `POST /auth/send-otp`
```json
{ "usernameOrEmail": "string (required)" }
```

---

### SetPasswordRequestDto
Used by `POST /auth/set-password` (identity resolved from otp_session cookie)
```json
{
  "otp": "string (required, 6-digit)",
  "newPassword": "string (required, min 8 chars)"
}
```

---

### ForgotUsernameRequestDto
Used by `POST /auth/forgot-username`
```json
{ "email": "string (required, valid email)" }
```

---

### VendorCreateDto
Used by `POST /vendors` (SUPER_ADMIN)
```json
{
  "username": "string (required)",
  "email": "string (required, valid email)",
  "companyName": "string (required)",
  "phone": "string (optional)",
  "description": "string (optional)"
}
```

---

### VendorUpdateDto
Used by `PUT /vendors/{id}` and `PUT /dashboard/vendor/{id}/profile`
```json
{
  "companyName": "string (required)",
  "phone": "string (optional)",
  "description": "string (optional)",
  "isActive": "boolean (optional)"
}
```

---

### PartnerCreateDto
Used by `POST /partners` (VENDOR)
```json
{
  "username": "string (required)",
  "email": "string (required, valid email)",
  "companyName": "string (required)",
  "phone": "string (optional)",
  "description": "string (optional)",
  "vendorId": "UUID string (optional, overridden by authenticated vendor)"
}
```

---

### PartnerUpdateDto
Used by `PUT /partners/{id}` and `PUT /dashboard/partner/{id}/profile`
```json
{
  "companyName": "string (required)",
  "phone": "string (optional)",
  "description": "string (optional)",
  "isActive": "boolean (optional)"
}
```

---

### CourseCreateDto / CourseUpdateDto
Used by `POST /courses` and `PUT /courses/{id}` (VENDOR)
```json
{
  "title": "string (required)",
  "subtitle": "string (optional)",
  "description": "string (required)",
  "category": "string (required)"
}
```

---

### BatchCreateDto
Used by `POST /batches` (VENDOR)
```json
{
  "courseId": "UUID (required)",
  "title": "string (required)",
  "subtitle": "string (optional)",
  "description": "string (required)",
  "startingDate": "date ISO (required)",
  "endingDate": "date ISO (required)"
}
```

---

### BatchUpdateDto
Used by `PUT /batches/{id}` (VENDOR)
```json
{
  "title": "string (required)",
  "subtitle": "string (optional)",
  "description": "string (required)",
  "startingDate": "date ISO (required)",
  "endingDate": "date ISO (required)"
}
```

---

### SessionCreateDto
Used by `POST /sessions` (VENDOR)
```json
{
  "courseId": "UUID (required)",
  "batchId": "UUID (required)",
  "sessionType": "CLASS|NOTE|ASSIGNMENT|PROJECT (required)",
  "title": "string (required)",
  "subtitle": "string (optional)",
  "description": "string (required)",
  "liveLink": "URL string (optional)",
  "recordedLink": "URL string (optional)",
  "resourceLink": "URL string (optional)",
  "scheduledDate": "date ISO (optional)",
  "scheduledTime": "HH:mm or HH:mm:ss (optional, 24hr format)"
}
```

---

### SessionUpdateDto
Used by `PUT /sessions/{id}` (VENDOR) — same fields as SessionCreateDto minus courseId and batchId.

---

### SessionReorderRequestDto
Used by `PUT /sessions/reorder` (VENDOR)
```json
{
  "courseId": "UUID (required)",
  "batchId": "UUID (required)",
  "sessionIds": [
    { "index": 1, "id": "UUID" },
    { "index": 2, "id": "UUID" }
  ]
}
```

---

### VendorAccessRequestCreateDto
Used by `POST /access-requests` (VENDOR creates request on behalf of partner)
```json
{
  "courseId": "UUID (required)",
  "batchId": "UUID (optional)",
  "courseName": "string (required)",
  "batchName": "string (optional)",
  "partnerId": "UUID (required)",
  "partnerName": "string (required)",
  "maxStudents": "integer (required)",
  "note": "string (optional)"
}
```

---

### PartnerAccessRequestCreateDto
Used by `POST /access-requests/request` (PARTNER self-requests access)
```json
{
  "courseId": "UUID (required)",
  "batchId": "UUID (optional)",
  "courseName": "string (required)",
  "batchName": "string (optional)",
  "maxStudents": "integer (optional)",
  "note": "string (optional)"
}
```

---

### AccessStatusUpdateDto
Used by `PUT /access-requests/{id}/status` (VENDOR)
```json
{
  "status": "APPROVED|REJECTED (required)",
  "responseNote": "string (optional)",
  "maxStudents": "integer (optional)"
}
```

---

### ComplaintCreateDto
Used by `POST /complaints` (VENDOR/PARTNER) or `POST /api/v1/open/sync/complaints` (public)
```json
{
  "studentId": "UUID (optional)",
  "partnerId": "UUID (required)",
  "vendorId": "UUID (required)",
  "courseId": "UUID (optional)",
  "batchId": "UUID (optional)",
  "studentName": "string (optional)",
  "email": "string (optional)",
  "phoneNumber": "string (optional)",
  "college": "string (optional)",
  "branch": "string (optional)",
  "academicYear": "string (optional)",
  "courseName": "string (optional)",
  "batchName": "string (optional)",
  "partnerName": "string (optional)",
  "vendorName": "string (optional)",
  "complaintTitle": "string (optional)",
  "complaintText": "string (optional)"
}
```

---

### ComplaintUpdateDto
Used by `PUT /complaints/{id}` (VENDOR/PARTNER)
```json
{
  "status": "PENDING|IN_PROGRESS|RESOLVED (optional)",
  "vendorRemark": "string (optional, only processed for VENDOR role)",
  "partnerRemark": "string (optional, only processed for PARTNER role)"
}
```

---

### StudentDataSyncRequest (external sync DTO)
Used by `POST /api/v1/open/sync/student-data`
```json
{
  "partnerId": "UUID",
  "students": [
    {
      "firstName": "string",
      "lastName": "string",
      "email": "string",
      "phoneNumber": "string",
      "college": "string",
      "branch": "string",
      "academicYear": "string",
      "courseId": "UUID",
      "batchId": "UUID",
      "courseName": "string",
      "batchName": "string"
    }
  ]
}
```

---

## 7. DTOs — Response Bodies

### ApiResponse (Universal Wrapper)
Every endpoint returns this shape:
```json
{
  "message": "string",
  "data": "T (generic, object/list/null)",
  "error": "HTTP status code string (only on errors)",
  "errorCode": "MACHINE_READABLE_CODE (only on errors)"
}
```

---

### LoginResponseDto
Returned by `POST /auth/login` and `GET /auth/session`. Also stored in Redis as session payload.
```json
{
  "user":    { ...UserDataDto },
  "vendor":  { ...VendorDataDto  (present for VENDOR and PARTNER roles) },
  "partner": { ...PartnerDataDto (present for PARTNER role only) }
}
```
Null fields are omitted from JSON output (`@JsonInclude(NON_NULL)`).

---

### UserDataDto
```json
{
  "id": "UUID string",
  "username": "string",
  "email": "string",
  "role": "SUPER_ADMIN|VENDOR|PARTNER",
  "status": "PENDING_SETUP|ACTIVE|BLOCKED",
  "emailVerified": "boolean",
  "lastLoginAt": "ISO datetime string",
  "createdAt": "ISO datetime string",
  "updatedAt": "ISO datetime string",
  "deviceDetails": { "deviceIp": "...", "deviceType": "...", "device": "...", "deviceModel": "...", "osName": "...", "osVersion": "...", "clientName": "...", "clientVersion": "..." }
}
```

---

### VendorDataDto (embedded in login response)
```json
{
  "id": "UUID string",
  "companyName": "string",
  "phone": "string",
  "description": "string",
  "active": "boolean",
  "createdAt": "ISO datetime string",
  "deviceDetails": { ... }
}
```

---

### PartnerDataDto (embedded in login response)
```json
{
  "id": "UUID string",
  "companyName": "string",
  "phone": "string",
  "description": "string",
  "active": "boolean",
  "vendorId": "UUID string",
  "createdAt": "ISO datetime string",
  "deviceDetails": { ... }
}
```

---

### VendorProfileResponseDto (full admin/vendor view)
```json
{
  "id": "UUID string",
  "username": "string",
  "email": "string",
  "companyName": "string",
  "phone": "string",
  "description": "string",
  "isActive": "boolean"
}
```

---

### PartnerProfileResponseDto (full profile with parent vendor)
```json
{
  "id": "UUID string",
  "username": "string",
  "email": "string",
  "companyName": "string",
  "phone": "string",
  "description": "string",
  "parentVendorId": "UUID string",
  "parentVendorCompanyName": "string",
  "isActive": "boolean"
}
```

---

### CourseDataDto (list view, includes access flags for Partners)
```json
{
  "id": "UUID",
  "title": "string",
  "subtitle": "string",
  "description": "string",
  "category": "string",
  "createdAt": "ISO datetime string",
  "updatedAt": "ISO datetime string",
  "hasRequestedAccess": "boolean",
  "accessStatus": "PENDING|APPROVED|REJECTED|null"
}
```

---

### CourseProfileResponseDto (detail view, no access flags)
```json
{
  "id": "UUID",
  "title": "string",
  "subtitle": "string",
  "description": "string",
  "category": "string",
  "createdAt": "ISO datetime string",
  "updatedAt": "ISO datetime string"
}
```

---

### BatchDataDto (list view, includes access flags)
```json
{
  "id": "UUID",
  "courseId": "UUID",
  "courseName": "string",
  "title": "string",
  "subtitle": "string",
  "description": "string",
  "startingDate": "ISO date",
  "endingDate": "ISO date",
  "createdAt": "ISO datetime string",
  "updatedAt": "ISO datetime string",
  "hasRequestedAccess": "boolean",
  "accessStatus": "PENDING|APPROVED|REJECTED|null"
}
```

---

### BatchProfileResponseDto (detail view, no access flags)
```json
{
  "id": "UUID",
  "courseId": "UUID",
  "title": "string",
  "subtitle": "string",
  "description": "string",
  "startingDate": "ISO date",
  "endingDate": "ISO date",
  "createdAt": "ISO datetime string",
  "updatedAt": "ISO datetime string"
}
```

---

### SessionDataDto / SessionProfileResponseDto
```json
{
  "id": "UUID",
  "courseId": "UUID",
  "batchId": "UUID",
  "sessionType": "CLASS|NOTE|ASSIGNMENT|PROJECT",
  "title": "string",
  "subtitle": "string",
  "description": "string",
  "liveLink": "string",
  "recordedLink": "string",
  "resourceLink": "string",
  "sequenceOrder": "integer",
  "scheduledDate": "ISO date",
  "scheduledTime": "HH:mm:ss",
  "createdAt": "ISO datetime",
  "updatedAt": "ISO datetime"
}
```

---

### PartnerAccessSessionDto (sessions for partner, links hidden unless APPROVED)
```json
{
  "id": "UUID",
  "courseId": "UUID",
  "courseName": "string",
  "batchId": "UUID",
  "batchName": "string",
  "title": "string",
  "subtitle": "string",
  "description": "string",
  "sessionType": "string",
  "sequenceOrder": "integer",
  "scheduledDate": "ISO date",
  "scheduledTime": "HH:mm",
  "liveLink": "string (null if not approved)",
  "recordedLink": "string (null if not approved)",
  "resourceLink": "string (null if not approved)",
  "hasRequestedAccess": "boolean",
  "accessStatus": "PENDING|APPROVED|REJECTED|null"
}
```

---

### AccessRequestResponseDto
```json
{
  "id": "UUID",
  "partnerId": "UUID",
  "vendorId": "UUID",
  "courseId": "UUID",
  "batchId": "UUID",
  "partnerName": "string",
  "courseName": "string",
  "batchName": "string",
  "status": "PENDING|APPROVED|REJECTED",
  "requestNote": "string",
  "responseNote": "string",
  "hasBatchAccess": "boolean",
  "maxStudents": "integer",
  "requestedAt": "ISO datetime",
  "resolvedAt": "ISO datetime"
}
```

---

### ComplaintResponseDto
```json
{
  "id": "UUID",
  "studentId": "UUID",
  "partnerId": "UUID",
  "vendorId": "UUID",
  "courseId": "UUID",
  "batchId": "UUID",
  "studentName": "string",
  "email": "string",
  "phoneNumber": "string",
  "college": "string",
  "branch": "string",
  "academicYear": "string",
  "courseName": "string",
  "batchName": "string",
  "partnerName": "string",
  "vendorName": "string",
  "complaintTitle": "string",
  "complaintText": "string",
  "status": "PENDING|IN_PROGRESS|RESOLVED",
  "vendorRemark": "string",
  "partnerRemark": "string",
  "isResolvedByVendor": "boolean",
  "isResolvedByPartner": "boolean",
  "createdAt": "ISO datetime",
  "updatedAt": "ISO datetime"
}
```

---

### VendorDashboardResponseDto
```json
{
  "totalCourses": "long",
  "totalBatches": "long",
  "totalPartners": "long",
  "totalStudents": "long",
  "pendingAccessRequests": "long",
  "recentComplaints": [ ...ComplaintResponseDto (top 5) ]
}
```

---

### PartnerDashboardResponseDto
```json
{
  "vendorDetails": { ...VendorProfileResponseDto },
  "totalStudents": "long",
  "activeEnrollments": "long",
  "recentComplaints": [ ...ComplaintResponseDto (top 5) ]
}
```

---

### PartnerSessionResponseDto (vendor portal view with usage data)
```json
{
  "id": "UUID",
  "courseId": "UUID",
  "courseName": "string",
  "batchId": "UUID",
  "batchName": "string",
  "sessionType": "string",
  "title": "string",
  "subtitle": "string",
  "description": "string",
  "liveLink": "string",
  "recordedLink": "string",
  "resourceLink": "string",
  "sequenceOrder": "integer",
  "scheduledDate": "ISO date",
  "scheduledTime": "HH:mm",
  "studentCount": "long (unique students from partner who accessed session)",
  "maxStudents": "integer (approved limit)",
  "limitExceeded": "boolean (studentCount > maxStudents)"
}
```

---

### PartnerStudentResponseDto
```json
{
  "id": "UUID",
  "firstName": "string",
  "lastName": "string",
  "fullName": "string",
  "email": "string",
  "phoneNumber": "string",
  "college": "string",
  "branch": "string",
  "academicYear": "string",
  "enrollments": [
    {
      "id": "UUID",
      "courseId": "UUID",
      "courseName": "string",
      "batchId": "UUID",
      "batchName": "string",
      "status": "ENROLLED|REMOVED|DELETED"
    }
  ]
}
```

---

### PartnerCalendarDayDto / PartnerCalendarSessionDto
```json
{
  "date": "ISO date",
  "sessions": [
    {
      "sessionId": "UUID",
      "courseId": "UUID",
      "courseName": "string",
      "batchId": "UUID",
      "batchName": "string",
      "title": "string",
      "subtitle": "string",
      "description": "string",
      "sequenceOrder": "integer",
      "scheduledDate": "ISO date",
      "scheduledTime": "HH:mm"
    }
  ]
}
```

---

### StudentDataSyncResponseDto
```json
{
  "createdCount": "integer",
  "updatedCount": "integer",
  "enrolledCount": "integer",
  "errors": [ "string" ]
}
```

---

## 8. Repositories

All repositories extend `JpaRepository<Entity, UUID>`. Most also extend `JpaSpecificationExecutor` for dynamic query filtering.

| Repository | Custom Methods |
|------------|---------------|
| UserRepository | findByUsername, findByEmail, existsByUsername, existsByEmail |
| VendorRepository | findByUserUsername |
| PartnerRepository | findByUserUsername, findByVendorUserUsername, findByVendorId, countByVendorId |
| CourseRepository | Standard JPA only |
| BatchRepository | findByCourseId, countByCourseId |
| SessionRepository | findByBatchIdOrderBySequenceOrderAsc, findMaxSequenceOrderByBatchId (JPQL), findByBatchIdsAndScheduledDateBetween (JPQL), countByBatchId |
| StudentRepository | findByPartnerId, countByPartnerId, countByPartnerIdIn, findByEmail |
| StudentEnrollmentRepository | findByStudentId, findByStudentIdAndCourseIdAndBatchId |
| StudentComplaintRepository | findByVendorId, findTop5ByVendorIdOrderByCreatedAtDesc, findByPartnerId, findTop5ByPartnerIdOrderByCreatedAtDesc |
| StudentSessionLogRepository | countDistinctStudentsBySessionIdAndPartnerId (JPQL), existsBySessionId, findByStudentIdAndSessionId |
| PartnerAccessRequestRepository | findByPartnerId, findByVendorId, countByVendorIdAndStatus |
| UserSessionRepository | findBySessionId, findByUserUsernameAndIsActiveTrue, invalidateAllSessionsForUser (JPQL bulk), deleteByUserId |
| ApiLogRepository | JpaSpecificationExecutor only |
| AuditLogRepository | JpaSpecificationExecutor only |

---

## 9. Security Layer

### JwtService
Handles JWT generation and validation using HMAC-SHA256.
- `generateToken(username, role)` — Creates a signed JWT with sub=username, role claim, unique jti (UUID), and configurable expiry.
- `extractUsername(token)` — Reads the sub claim.
- `extractSessionId(token)` — Reads the jti claim (used as Redis key).
- `isTokenValid(token, username)` — Validates both signature and expiry.
- Config from: `application.security.jwt.secret-key` and `application.security.jwt.expiration`.

### JwtAuthenticationFilter (extends OncePerRequestFilter)
The main security gatekeeper. On every request:
1. Reads the `session_id` HttpOnly cookie from the request.
2. Looks up the session data (LoginResponseDto) from Redis using the cookie value.
3. If found and valid, populates SecurityContextHolder with a UsernamePasswordAuthenticationToken using the username and role.
4. Handles Redis connection errors gracefully — logs error, does not block the filter chain.
5. Detailed DEBUG logging at each step (with emojis) for easy log scanning.

### CustomUserDetailsService
Implements Spring Security's UserDetailsService. Loads a UserPrincipal by username from the database. Used by Spring Security's AuthenticationManager for the credential-based login step.

### UserPrincipal
Wraps the User entity to implement UserDetails:
- `getAuthorities()` — Returns the raw role name (e.g., "SUPER_ADMIN", "VENDOR") as a SimpleGrantedAuthority.
- `isAccountNonLocked()` — Returns false for BLOCKED users.
- `isEnabled()` — Returns true only for ACTIVE users.

### CookieService
Creates and clears HttpOnly, SameSite=Strict cookies:
- `createCookie(name, value, maxAgeSeconds)` — Returns a ResponseCookie with secure attributes.
- `clearCookie(name)` — Creates a cookie with maxAge=0 to instruct the browser to delete it.

---

## 10. Configuration Files

### AdminSeeder
Runs on application startup (CommandLineRunner). If no user with the configured admin username exists, creates a SUPER_ADMIN user with status ACTIVE. Credentials from application.yml:
- `app.admin.username` (default: admin)
- `app.admin.email` (default: admin@example.com)
- `app.admin.password` (default: ChangeMe@123)

### ApiLoggingFilter (Servlet Filter)
Intercepts every HTTP request after Spring Security processes it:
1. **Rate limiting** — Checks Redis. Rejects with HTTP 429 if IP exceeds configured threshold.
2. **Body caching** — Wraps request/response in ContentCachingRequestWrapper / ContentCachingResponseWrapper.
3. **Async logging** — After response completes, builds an ApiLog and saves it via a thread executor (no response latency impact).
4. Masks session_id cookie (shows only first 8 chars + ***[MASKED]).
5. Truncates request/response bodies to 5000 characters.
6. Skipped for Swagger UI and OpenAPI spec paths.

### RedisConfig
Configures a `RedisTemplate<String, Object>` bean with:
- StringRedisSerializer for keys.
- GenericJackson2JsonRedisSerializer for values (JSON-based storage).

### OpenApiConfig
Configures Swagger UI via SpringDoc. Defines a global JWT Bearer token security scheme so every endpoint shows an "Authorize" button. Title: "Bleep LearnHub API", Version: "1.0".

### MailConfig
Configures the JavaMailSender bean. SMTP credentials from `spring.mail.*` in application.yml.

### RequestTraceFilter / MasterLoggingFilter
Injects a correlationId (UUID) into the MDC (Mapped Diagnostic Context) for log correlation. Every log line for a given HTTP request includes the same correlation ID.

### SecurityConfig
Currently commented out. The application relies on JwtAuthenticationFilter for authentication and @PreAuthorize annotations for authorization instead of a SecurityFilterChain bean. The existing commented code shows the intended config: CSRF disabled, stateless sessions, /auth/** public.

### LayerLoggingAspect
Intentionally disabled and empty. Was an AOP-based logging aspect but caused CGLIB proxy conflicts with servlet filters. Replaced by direct @Slf4j log statements in each layer.

---

## 11. Exception Handling

GlobalExceptionHandler (@RestControllerAdvice) catches all exceptions globally.

| Exception | HTTP Status | Error Code | When Triggered |
|-----------|-------------|-----------|----------------|
| MethodArgumentNotValidException | 400 | VALIDATION_ERROR | DTO field validation fails (@NotBlank, @Email, etc.) |
| ResourceNotFoundException | 404 | RESOURCE_NOT_FOUND | Entity not found in database |
| BusinessException | 400 | BUSINESS_RULE_VIOLATION | Business logic constraint violated |
| BadCredentialsException | 400 | BAD_CREDENTIALS | Wrong username/password |
| DisabledException | 403 | ACCOUNT_NOT_ACTIVE | PENDING_SETUP user tries to log in |
| LockedException | 403 | ACCOUNT_BLOCKED | BLOCKED user tries to log in |
| AccessDeniedException | 403 | ACCESS_DENIED | Authenticated user lacks required role |
| DataIntegrityViolationException | 400 | DATA_INTEGRITY_VIOLATION | DB unique constraint or FK violation |
| Exception (fallback) | 500 | INTERNAL_SERVER_ERROR | Any other unhandled exception |

Validation errors return a field-level error map:
```json
{
  "message": "Validation Failed",
  "errorCode": "VALIDATION_ERROR",
  "data": {
    "email": "Must be a valid email address",
    "title": "Title is required"
  }
}
```

---

## 12. Constants

### CookieConstants
```
SESSION_ID      = "session_id"    Cookie name for the login session
SESSION_AGE     = 604800L         7 days in seconds
OTP_SESSION     = "otp_session"   Cookie name for the OTP flow
OTP_SESSION_AGE = 1800L           30 minutes in seconds
```

---

## 13. Services

### AuthService
Core authentication service.

| Method | Description |
|--------|-------------|
| login(dto, deviceDetails) | Validates credentials, creates JWT, stores LoginResponseDto in Redis keyed by JWT jti, saves UserSession audit record, updates lastLoginAt, returns session key + profile data |
| getSession(sessionId) | Retrieves LoginResponseDto from Redis (no DB hit) |
| logout(sessionId) | Deletes Redis session, marks UserSession.isActive=false, sets logoutAt |
| sendOtp(usernameOrEmail) | Rate-limits (max N OTPs per time window per user), generates 6-digit OTP, stores OtpSessionData in Redis with a random token key, sends OTP email async, returns the token |
| setPassword(otpToken, otp, newPassword) | Retrieves OTP data from Redis, validates OTP, BCrypt-hashes new password, sets AccountStatus=ACTIVE, emailVerified=true, deletes Redis OTP key |
| forgotUsername(email) | Finds user by email, sends username via email — always returns 200 to prevent enumeration |
| getAllUsers() | Returns all users as UserDataDto list |

---

### VendorService

| Method | Description |
|--------|-------------|
| createVendor(dto) | Validates uniqueness, creates User (VENDOR role, PENDING_SETUP), creates Vendor profile, sends welcome email async |
| getAllVendors() | Returns all vendors as VendorProfileResponseDto list |
| getVendorById(id) | Returns single vendor profile |
| updateVendor(id, dto) | Updates vendor profile fields |
| deleteVendor(id) | Validates no partners exist, deletes vendor + user |

---

### PartnerService

| Method | Description |
|--------|-------------|
| createPartner(dto, vendorUsername) | Resolves calling vendor, creates User (PARTNER role, PENDING_SETUP), creates Partner under that vendor, sends welcome email |
| getAllPartners(vendorUsername) | Returns all partners under the calling vendor |
| getPartnerById(id) | Returns single partner profile with parent vendor data |
| updatePartner(id, dto) | Updates partner profile fields |
| deletePartner(id) | Deletes partner + user record |

---

### CourseService

| Method | Description |
|--------|-------------|
| createCourse(dto) | Creates a new Course |
| updateCourse(id, dto) | Updates course fields |
| deleteCourse(id) | Validates no batches exist, then deletes |
| getAllCourses() | Returns all courses |
| getCourseById(id) | Returns single course profile |

---

### BatchService

| Method | Description |
|--------|-------------|
| createBatch(dto) | Validates course exists, creates Batch |
| updateBatch(id, dto) | Updates batch fields |
| deleteBatch(id) | Validates no sessions exist, then deletes |
| getBatchesByCourseId(courseId) | Returns batches for a course |
| getAllBatches() | Returns all batches |
| getBatchById(id) | Returns single batch profile |

---

### SessionService

| Method | Description |
|--------|-------------|
| createSession(dto) | Auto-assigns sequenceOrder = max + 1 within the batch, creates Session |
| updateSession(id, dto) | Updates session fields |
| deleteSession(id) | Deletes session |
| getAllSessions(courseId, batchId, type, search, sortOrder) | Filterable session list |
| getSessionById(id) | Returns session profile |
| reorderSessions(request) | Bulk-updates sequenceOrder for a list of sessions |

---

### PartnerAccessRequestService

| Method | Description |
|--------|-------------|
| createAccessRequestByVendor(dto, vendorUsername) | Vendor creates access grant for a partner |
| createAccessRequestByPartner(dto, partnerUsername) | Partner self-requests access |
| updateAccessStatus(id, dto) | Vendor approves or rejects a request |
| deleteAccessRequest(id) | Deletes an access record |
| getRequestsByVendorId(vendorId) | All requests for a vendor |
| getRequestsByPartnerId(partnerId) | All requests for a partner |

---

### PartnerAccessService
Handles the partner's perspective when browsing content.

| Method | Description |
|--------|-------------|
| getCourses(partnerId, username) | All courses flagged with hasRequestedAccess and accessStatus |
| getBatches(courseId, partnerId, username) | Batches for a course with access flags |
| getSessions(courseId, batchId, partnerId, username) | Sessions; live/recorded/resource links are null unless APPROVED |
| getSchedule(fromDate, toDate, partnerId, username) | Day-by-day calendar of scheduled sessions for all approved batches |

---

### PartnerPortalService
Vendor's view into a specific partner's content (used in Vendor portal).

| Method | Description |
|--------|-------------|
| getCoursesByPartnerId(partnerId) | Courses accessible by the partner |
| getBatchesByPartnerAndCourse(partnerId, courseId) | Accessible batches |
| getSessionsByBatchAndPartner(partnerId, batchId) | Sessions with student count vs limit |
| getStudentsByPartner(partnerId) | Students registered under partner with enrollments |
| getLimitCrossedSessionsByPartner(partnerId) | Sessions where studentCount > maxStudents |

---

### StudentComplaintService

| Method | Description |
|--------|-------------|
| createComplaint(dto) | Creates a complaint with status PENDING |
| updateComplaint(id, dto, isVendor, isPartner) | Role-aware: vendor can set vendorRemark, partner can set partnerRemark |
| deleteComplaint(id) | Deletes complaint |
| getComplaintsByVendorId(vendorId) | All complaints for a vendor |
| getComplaintsByPartnerId(partnerId) | All complaints for a partner |
| getComplaintById(id) | Single complaint |

---

### StudentDataSyncService
External data ingestion service (used by mobile apps / external systems).

`syncStudentData(request)` — For each student in the request:
- If student email exists: update all fields.
- If not: create new Student record.
- For each enrollment in the request: upsert StudentEnrollment (ENROLLED status).
- Returns StudentDataSyncResponseDto with counts of created, updated, enrolled, and any per-row errors.

---

### DashboardServiceImpl (implements DashboardService)

| Method | Description |
|--------|-------------|
| getVendorDashboard(vendorId) | Counts: courses, batches, partners, students (across all partners), pending access requests; returns top 5 recent complaints |
| getPartnerDashboard(partnerId) | Returns parent vendor profile, total students, active enrollments, top 5 recent complaints |

---

### RedisService
Centralizes all Redis interactions.

| Method | Description |
|--------|-------------|
| saveSession(sessionId, data) | Stores LoginResponseDto in Redis with 7-day TTL |
| getSessionData(sessionId) | Retrieves session data |
| deleteSession(sessionId) | Removes session |
| saveOtpSession(token, data) | Stores OtpSessionData with 30-min TTL |
| getOtpSession(token) | Retrieves OTP data |
| deleteOtpSession(token) | Removes OTP data (after successful use) |
| getOtpCount(username) | Gets OTP request count for rate limiting |
| incrementOtpCount(username) | Increments count with TTL |
| allowApiCall(ip, maxRequests, timeFrameMinutes) | Sliding-window rate limit per IP |

---

### EmailService
All methods are @Async — they run on a separate thread pool.

| Method | Description |
|--------|-------------|
| sendWelcomeEmail(to, username, role) | Sent when Vendor or Partner is onboarded |
| sendOtpEmail(to, otp) | Sent during OTP request flow (includes expiry info) |
| sendForgotUsernameEmail(to, username) | Sent when user requests forgotten username |

---

### MasterDataService
Admin utility service. Provides paginated GET + DELETE (single and all) for every entity table. All operations are exposed via MasterDataController (SUPER_ADMIN only).

---

## 14. Controllers & API Reference

**Authentication:** All endpoints except /auth/** and /api/v1/open/** require a valid `session_id` cookie.

---

### AuthController — Base path: `/auth`

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| POST | /auth/login | No | — | Login with username+password. Sets session_id HttpOnly cookie. |
| GET | /auth/session | Cookie | Any | Validate session; returns LoginResponseDto from Redis. |
| POST | /auth/logout | Cookie | Any | Deletes Redis session, clears session_id cookie. |
| POST | /auth/send-otp | No | — | Request 6-digit OTP to registered email. Sets otp_session cookie. Rate-limited. |
| POST | /auth/set-password | No (uses otp_session cookie) | — | Validate OTP and set new password. Activates account. Clears otp_session cookie. |
| POST | /auth/forgot-username | No | — | Send username to registered email. Always returns 200. |
| GET | /auth/users | Cookie | SUPER_ADMIN | List all users. |
| GET | /auth/user-list | Cookie | SUPER_ADMIN | Alias for /auth/users. |

---

### VendorController — Base path: `/vendors`
All endpoints require SUPER_ADMIN.

| Method | Path | Description | Request Body | Response |
|--------|------|-------------|--------------|----------|
| POST | /vendors | Create Vendor + User. Sends welcome email. | VendorCreateDto | ApiResponse<Void> |
| GET | /vendors | List all vendors. | — | ApiResponse<List<VendorProfileResponseDto>> |
| GET | /vendors/{id} | Get vendor by ID. | — | ApiResponse<VendorProfileResponseDto> |
| PUT | /vendors/{id} | Update vendor profile. | VendorUpdateDto | ApiResponse<Void> |
| DELETE | /vendors/{id} | Delete vendor (fails if partners exist). | — | ApiResponse<Void> |

---

### PartnerController — Base path: `/partners`
All endpoints require VENDOR.

| Method | Path | Description | Request Body | Response |
|--------|------|-------------|--------------|----------|
| POST | /partners | Create Partner under calling vendor. | PartnerCreateDto | ApiResponse<Void> |
| GET | /partners | List all partners under calling vendor. | — | ApiResponse<List<PartnerProfileResponseDto>> |
| GET | /partners/{id} | Get partner by ID. | — | ApiResponse<PartnerProfileResponseDto> |
| PUT | /partners/{id} | Update partner profile. | PartnerUpdateDto | ApiResponse<Void> |
| DELETE | /partners/{id} | Delete partner and associated user. | — | ApiResponse<Void> |

---

### CourseController — Base path: `/courses`

| Method | Path | Role | Description | Request Body | Response |
|--------|------|------|-------------|--------------|----------|
| POST | /courses | VENDOR | Create a course. | CourseCreateDto | ApiResponse<CourseProfileResponseDto> |
| PUT | /courses/{id} | VENDOR | Update a course. | CourseUpdateDto | ApiResponse<Void> |
| DELETE | /courses/{id} | VENDOR | Delete course (fails if batches exist). | — | ApiResponse<Void> |
| GET | /courses | VENDOR, PARTNER | List all courses. | — | ApiResponse<List<CourseDataDto>> |
| GET | /courses/{id} | VENDOR, PARTNER | Get course by ID. | — | ApiResponse<CourseProfileResponseDto> |

---

### BatchController — Base path: `/batches`

| Method | Path | Role | Description | Request Body | Response |
|--------|------|------|-------------|--------------|----------|
| POST | /batches | VENDOR | Create a batch. | BatchCreateDto | ApiResponse<BatchProfileResponseDto> |
| PUT | /batches/{id} | VENDOR | Update batch. | BatchUpdateDto | ApiResponse<Void> |
| DELETE | /batches/{id} | VENDOR | Delete batch (fails if sessions exist). | — | ApiResponse<Void> |
| GET | /batches | VENDOR, PARTNER | List batches. Query param: ?courseId= | — | ApiResponse<List<BatchDataDto>> |
| GET | /batches/{id} | VENDOR, PARTNER | Get batch by ID. | — | ApiResponse<BatchProfileResponseDto> |

---

### SessionController — Base path: `/sessions`

| Method | Path | Role | Description | Request Body | Response |
|--------|------|------|-------------|--------------|----------|
| POST | /sessions | VENDOR | Create session (auto-assigns sequence). | SessionCreateDto | ApiResponse<SessionDataDto> |
| PUT | /sessions/{id} | VENDOR | Update session. | SessionUpdateDto | ApiResponse<Void> |
| DELETE | /sessions/{id} | VENDOR | Delete session. | — | ApiResponse<Void> |
| GET | /sessions | VENDOR, PARTNER | List sessions. Params: ?courseId, ?batchId, ?type, ?search, ?sortOrder | — | ApiResponse<List<SessionDataDto>> |
| GET | /sessions/{id} | VENDOR, PARTNER | Get session by ID. | — | ApiResponse<SessionProfileResponseDto> |
| PUT | /sessions/reorder | VENDOR | Bulk reorder sessions by sequence. | SessionReorderRequestDto | ApiResponse<Void> |

---

### PartnerAccessRequestController — Base path: `/access-requests`

| Method | Path | Role | Description | Request Body | Response |
|--------|------|------|-------------|--------------|----------|
| POST | /access-requests | VENDOR | Vendor creates access grant for a partner. | VendorAccessRequestCreateDto | ApiResponse<Void> |
| POST | /access-requests/request | PARTNER | Partner self-requests access to course/batch. | PartnerAccessRequestCreateDto | ApiResponse<Void> |
| PUT | /access-requests/{id}/status | VENDOR | Approve or reject a request. | AccessStatusUpdateDto | ApiResponse<Void> |
| DELETE | /access-requests/{id} | VENDOR | Delete an access record. | — | ApiResponse<Void> |
| GET | /access-requests/vendor/{vendorId} | VENDOR, SUPER_ADMIN | All access requests for a vendor. | — | ApiResponse<List<AccessRequestResponseDto>> |
| GET | /access-requests/partner/{partnerId} | VENDOR, SUPER_ADMIN | All access requests for a partner. | — | ApiResponse<List<AccessRequestResponseDto>> |

---

### PartnerAccessController — Base path: `/partner-access`
All endpoints require PARTNER role. Partners use these to browse content with their access permissions applied.

| Method | Path | Description | Query Params | Response |
|--------|------|-------------|-------------|----------|
| GET | /partner-access/courses | List all courses with access status flags. | partnerId (optional) | ApiResponse<List<CourseDataDto>> |
| GET | /partner-access/batches | List batches of a course with access flags. | courseId, partnerId (optional) | ApiResponse<List<BatchDataDto>> |
| GET | /partner-access/sessions | List sessions for a batch. Links hidden if not approved. | courseId, batchId, partnerId (optional) | ApiResponse<List<PartnerAccessSessionDto>> |
| GET | /partner-access/schedule | Day-by-day calendar of scheduled sessions. | fromDate, toDate, partnerId | ApiResponse<List<PartnerCalendarDayDto>> |

---

### PartnerPortalController — Base path: `/partner-portal`
All endpoints require VENDOR role. Vendor views what a specific Partner can see.

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| GET | /partner-portal/partner/{partnerId}/courses | Courses accessible by the partner. | ApiResponse<List<CourseDataDto>> |
| GET | /partner-portal/partner/{partnerId}/courses/{courseId}/batches | Accessible batches. | ApiResponse<List<BatchDataDto>> |
| GET | /partner-portal/partner/{partnerId}/batches/{batchId}/sessions | Sessions with student count vs limit. | ApiResponse<List<PartnerSessionResponseDto>> |
| GET | /partner-portal/partner/{partnerId}/students | Students under partner with enrollments. | ApiResponse<List<PartnerStudentResponseDto>> |
| GET | /partner-portal/partner/{partnerId}/sessions/limit-crossed | Sessions where studentCount > maxStudents. | ApiResponse<List<PartnerSessionResponseDto>> |

---

### DashboardController — Base path: `/dashboard`

| Method | Path | Role | Description | Response |
|--------|------|------|-------------|----------|
| GET | /dashboard/vendor/{vendorId} | SUPER_ADMIN, VENDOR | Vendor dashboard stats. Vendor can only see own dashboard. | ApiResponse<VendorDashboardResponseDto> |
| GET | /dashboard/partner/{partnerId} | SUPER_ADMIN, PARTNER | Partner dashboard stats. Partner can only see own dashboard. | ApiResponse<PartnerDashboardResponseDto> |
| GET | /dashboard/vendor/{vendorId}/profile | VENDOR | Vendor's own profile (data isolation enforced). | ApiResponse<VendorProfileResponseDto> |
| PUT | /dashboard/vendor/{vendorId}/profile | VENDOR | Update own vendor profile (data isolation enforced). | ApiResponse<Void> |
| GET | /dashboard/partner/{partnerId}/profile | PARTNER | Partner's own profile (data isolation enforced). | ApiResponse<PartnerProfileResponseDto> |
| PUT | /dashboard/partner/{partnerId}/profile | PARTNER | Update own partner profile (data isolation enforced). | ApiResponse<Void> |

---

### StudentComplaintController — Base path: `/complaints`

| Method | Path | Role | Description | Request Body | Response |
|--------|------|------|-------------|--------------|----------|
| POST | /complaints | VENDOR, PARTNER | Submit a new complaint. | ComplaintCreateDto | ApiResponse<Void> |
| PUT | /complaints/{id} | VENDOR, PARTNER | Update status/remark (role-aware). | ComplaintUpdateDto | ApiResponse<Void> |
| DELETE | /complaints/{id} | VENDOR, PARTNER | Delete a complaint. | — | ApiResponse<Void> |
| GET | /complaints | VENDOR, PARTNER | List complaints (?vendorId or ?partnerId). | — | ApiResponse<List<ComplaintResponseDto>> |
| GET | /complaints/{id} | VENDOR, PARTNER | Get single complaint. | — | ApiResponse<ComplaintResponseDto> |

---

### OpenDataSyncController — Base path: `/api/v1/open/sync`
No authentication required. Used by external mobile apps to push data.

| Method | Path | Description | Request Body | Response |
|--------|------|-------------|--------------|----------|
| POST | /api/v1/open/sync/student-data | Upsert student records and enrollments from external app. | StudentDataSyncRequest | ApiResponse<StudentDataSyncResponseDto> |
| POST | /api/v1/open/sync/complaints | Submit a complaint anonymously. | ComplaintCreateDto | ApiResponse<Void> |
| GET | /api/v1/open/sync/courses | Courses accessible by a partner. | ?partnerId=UUID | ApiResponse<List<CourseDataDto>> |
| GET | /api/v1/open/sync/courses/{courseId}/batches | Batches for partner + course. | ?partnerId=UUID | ApiResponse<List<BatchDataDto>> |

---

### ApiLogController — Base path: `/api-logs`
All endpoints require SUPER_ADMIN.

| Method | Path | Description |
|--------|------|-------------|
| GET | /api-logs | Paginated list of API logs. Params: page, size, search. |
| DELETE | /api-logs/{id} | Delete a single API log. |
| DELETE | /api-logs | Delete all API logs. |

---

### AuditLogController — Base path: `/audit-logs`
All endpoints require SUPER_ADMIN.

| Method | Path | Description |
|--------|------|-------------|
| GET | /audit-logs | Paginated list of audit logs. Params: page, size, search. |
| DELETE | /audit-logs/{id} | Delete a single audit log. |
| DELETE | /audit-logs | Delete all audit logs. |

---

### MasterDataController — Base path: `/master-data`
All endpoints require SUPER_ADMIN. Raw database table access for admin/debugging. Entities: ApiLog, AuditLog, Batch, Course, Partner, PartnerAccessRequest, Session, Student, StudentComplaint, StudentEnrollment, StudentSessionLog, User, UserSession, Vendor.

Pattern for each entity:
- `GET /master-data/{entity-plural}` — Paginated list with optional search.
- `DELETE /master-data/{entity-plural}/{id}` — Delete single record.
- `DELETE /master-data/{entity-plural}` — Delete all records.

---

## 15. Cross-Cutting Concerns

### API Logging
Every HTTP request creates an ApiLog record. Key behaviors:
- **Async writing** — Saved on a separate thread pool, zero latency impact on responses.
- **IP-based rate limiting** — Checked before processing; returns HTTP 429 if threshold exceeded.
- **Body capture** — Request and response bodies cached and truncated to 5000 chars.
- **Session masking** — Only first 8 chars of session cookie stored.
- **Skipped paths** — Swagger and OpenAPI docs do not produce API logs.

### Request Tracing
A correlationId (UUID) is injected into MDC on every request via RequestTraceFilter. All log lines for a single HTTP request can be correlated in log aggregation tools (ELK, Loki, etc.).

---

## 16. Redis Usage

| Purpose | Key Pattern | TTL | Value |
|---------|-------------|-----|-------|
| Login Session | session:{sessionId} | 7 days | LoginResponseDto (JSON) |
| OTP Session | otp:{token} | 30 minutes | OtpSessionData (JSON) |
| OTP Rate Limit | otp_count:{username} | 30 minutes | Integer counter |
| API Rate Limit | rate:{ipAddress} | Configured window | Integer counter |

The JwtAuthenticationFilter reads the session from Redis on every authenticated request. Redis is the source of authentication truth, not the JWT signature itself. The JWT is only used to extract the session key (jti claim) which looks up the full session data in Redis.

---

## 17. Email System

All emails are sent asynchronously via Spring's @Async. Failures are logged but never propagated to the caller.

| Email Type | Trigger | Subject |
|-----------|---------|---------|
| Welcome | Vendor or Partner created | "Welcome to Bleep LearnHub!" |
| OTP | send-otp endpoint called | "Your Bleep LearnHub OTP" |
| Forgot Username | forgot-username endpoint called | "Your Bleep LearnHub Username" |

OTP expiry time is configurable via `app.otp.time-frame-minutes` and is included in the email body.

---

## 18. Data Isolation & Permission Model

Strict data isolation is enforced at the service and controller level:

| Scenario | How It Is Enforced |
|----------|--------------------|
| Vendor sees only own partners | PartnerService filters by calling user's vendorId |
| Vendor sees only own dashboard | DashboardController compares path vendorId with caller's vendorId |
| Partner sees only own dashboard | Same pattern in DashboardController |
| Partner sees approved content only | PartnerAccessService filters; session links are null unless APPROVED |
| SUPER_ADMIN bypasses all isolation | Explicit isSuperAdmin check in DashboardController |
| Session student limit enforcement | PartnerSessionResponseDto.limitExceeded flag set when studentCount > maxStudents |

Role-based endpoint guards use @PreAuthorize annotations:
- `@PreAuthorize("hasAuthority('SUPER_ADMIN')")` — Admin only
- `@PreAuthorize("hasAuthority('VENDOR')")` — Vendor only
- `@PreAuthorize("hasAuthority('PARTNER')")` — Partner only
- `@PreAuthorize("hasAnyAuthority('VENDOR', 'PARTNER')")` — Either
- `@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'VENDOR')")` — Admin or Vendor

The @PreAuthorize evaluation is fed by the SecurityContextHolder, which is populated by JwtAuthenticationFilter based on Redis session data.
