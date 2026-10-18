# PROJECT MEMORY — Bleep-LearnHub-Backend

> Single source of truth for this project. Read this before doing any work here.
> Stack: **Java 21 · Spring Boot 4.1 · PostgreSQL · Redis · Maven**

---

## 1. What This Project Does

**Bleep-LearnHub-Backend** is the Spring Boot REST API powering **Bleep LearnHub ("Training Center" / `b2b.trainingcentral.in`)** — a multi-tenant B2B training/LMS platform.

It serves three portals (frontends live in separate repos):

| Role | Who | What they do |
|---|---|---|
| **SUPER_ADMIN** | Platform owner (Bleep) | Manages vendors, partners, all master-data tables, API/audit logs |
| **VENDOR** | Training company | Creates **Courses → Batches → Sessions** (CLASS / NOTE / ASSIGNMENT / PROJECT), onboards partners, grants batch access, broadcasts notifications |
| **PARTNER** | College/institution | Browses the vendor's catalog, requests batch access (with max-student seat limit), manages students, receives SSE notifications, handles complaints |

**Students never log in** — they are data rows synced in through public `/open/sync/**` endpoints (used by the shareable "Join Live Class" and "Support Center" links from the frontend).

### Ecosystem (how the 3 projects fit together)

```
Internet → https://b2b.trainingcentral.in (prod) / https://staging.trainingcentral.in (staging)
        → Bleep-Traefik-Manager (Traefik v3 reverse proxy, VM 200.97.175.94)
             ├─ "/*"                              → Bleep-LearnHub-Frontend (3000 prod / 3001 staging)
             └─ "/bleep-learnhub-backend/api/v1"  → THIS PROJECT   (8080 prod / 8081 staging)
```

---

## 2. Tech Stack & Dependencies (`pom.xml`)

| Item | Version | Purpose |
|---|---|---|
| Spring Boot parent | **4.1.0** | Framework |
| Java | **21** | Language |
| `spring-boot-starter-webmvc` | (BOM) | REST endpoints |
| `spring-boot-starter-data-jpa` | (BOM) | Persistence (Hibernate) |
| `spring-boot-starter-data-redis` | (BOM) | Sessions, OTP, rate limiting (Lettuce) |
| `spring-boot-starter-security` | (BOM) | Auth (cookie sessions + roles) |
| `spring-boot-starter-validation` | (BOM) | Bean Validation |
| `spring-boot-starter-mail` | (BOM) | SMTP email (Hostinger, `smtp.hostinger.com:465`) |
| `spring-boot-starter-actuator` | (BOM) | Health/metrics |
| `org.postgresql:postgresql` | (runtime) | PostgreSQL driver |
| `io.jsonwebtoken:jjwt-*` | 0.12.5 | JWT utility (**legacy** — live auth is Redis sessions) |
| `org.springdoc:...webmvc-ui` | 2.8.5 | OpenAPI 3 + Swagger UI |
| `org.flywaydb:flyway-core` + postgresql | (BOM) | DB migrations |
| `org.projectlombok:lombok` | (optional) | Boilerplate reduction |
| `org.codehaus.janino:janino` | (BOM) | `<if>` conditionals in logback |

---

## 3. How to Run

```bash
# Dev (needs local PostgreSQL on :5433 and Redis on :6380, or use docker/)
mvn spring-boot:run                 # app on http://localhost:8080
mvn clean package -DskipTests       # build fat JAR

# Swagger UI
http://localhost:8080/bleep-learnhub-backend/api/v1/swagger-ui/index.html

# Deploy (build Docker image tarball, scp to VM, docker load)
./build.sh staging|prod
./deploy.sh staging|prod            # VM: root@200.97.175.94
```

**Base path of every endpoint:** `/bleep-learnhub-backend/api/v1`
**Ports:** app 8080 (container) · prod host 8080 · staging host 8081 · PostgreSQL 5432 (dev compose 5433) · Redis 6380
**Databases:** `learnhub_db` (prod) · `learnhub_db_staging` (staging) · Redis DB 0 prod / 1 staging

---

## 4. Project Structure

```
src/main/java/com/bleep/learnhub/
├── BleepLearnhubBackendApplication.java   @SpringBootApplication @EnableAsync @EnableScheduling
├── config/       AdminSeeder, ApiLoggingFilter, MasterLoggingFilter, MailConfig,
│                 OpenApiConfig, RedisConfig, SecurityConfig
├── constants/    CookieConstants
├── controller/   18 controllers (see §7)
├── dto/          request/, response/, Notification*, OtpSessionData, StudentDataSyncRequest...
├── entity/       15 JPA entities + enums/ (8 enums)
├── event/        NotificationCreatedEvent
├── exception/    BusinessException, ResourceNotFoundException, GlobalExceptionHandler
├── repository/   16 Spring Data repositories
├── security/     JwtService (legacy), JwtAuthenticationFilter, CustomUserDetailsService,
│                 UserPrincipal, CookieService
├── service/      21 services (+impl/DashboardServiceImpl)
└── sse/          SseConnectionManager
src/main/resources/
├── application.properties       (single file; env-vars override per environment)
├── logback-spring.xml
└── db/migration/V1__add_end_time_to_sessions.sql
```

Root: `Dockerfile` (multi-stage, temurin-21, non-root), `docker-compose.yml`, `build.sh`, `deploy.sh`, `vmsetup.sh`, `.env.prod`, `.env.staging`, docs: `API_FLOW.md` (⚠️ outdated — describes JWT Bearer; actual auth is cookie+Redis), `PROJECT_STRUCTURE.md`, `sql-migrations.md`, `projectInfo/`, `project-memory/`. Meta files: `PROJECT_MEMORY.md` (this doc), `opencode.jsonc` (OpenCode permission config — auto-loaded as instructions), `session/SESSION_LOG.md` (work journal — one entry per session, newest first).

---

## 5. Configuration (key properties)

| Property | Dev | Prod / Staging |
|---|---|---|
| `server.servlet.context-path` | `/bleep-learnhub-backend/api/v1` | same |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5433/learnhub_db` | `host.docker.internal:5432/learnhub_db[_staging]` |
| `spring.jpa.hibernate.ddl-auto` | `none` | `update` |
| Redis | `localhost:6380`, database 1 | `host.docker.internal:6380`, DB 0 (prod) / 1 (staging) |
| `app.session.timeout-seconds` | 3600 (1 h) | same |
| `app.api.rate-limit.max-requests` | 200 / min / IP | same |
| `app.otp.max-requests` | 100 / 30 min | **5** / 30 min |
| Mail | Hostinger SMTP 465 SSL | same (`mail.debug=false`) |
| Flyway | enabled, `baseline-on-migrate=true` | same |
| Admin seeder | `app.admin.*` seeds SUPER_ADMIN on first boot | ⚠️ credentials in properties |

---

## 6. Database Schema (PostgreSQL — all PKs are UUIDs)

> ⚠️ `projectInfo/Database_Schema.md` is slightly stale — **entity code is authoritative**. Cross-reference columns (enrollments, logs, complaints) have indexes but **no DB-level foreign keys** (denormalized name snapshots).

### Enums
`Role` (SUPER_ADMIN, VENDOR, PARTNER) · `AccountStatus` (PENDING_SETUP, ACTIVE, BLOCKED) · `SessionType` (CLASS, NOTE, ASSIGNMENT, PROJECT) · `EnrollmentStatus` (ENROLLED, REMOVED, DELETED) · `AccessRequestStatus` (PENDING, APPROVED, REJECTED) · `ComplaintStatus` (PENDING, IN_PROGRESS, RESOLVED) · `NotificationPriority` (LOW, NORMAL, HIGH, URGENT) · `NotificationTargetType` (ALL_PARTNERS, COURSE, BATCH, SPECIFIC_PARTNERS)

### users
| Column | Type | Notes |
|---|---|---|
| id | UUID | PK |
| username | VARCHAR(100) | unique, not null |
| email | VARCHAR(255) | unique, not null |
| password_hash | TEXT | null until OTP setup |
| role / status | enum string | not null |
| email_verified | boolean | default false |
| last_login_at | TIMESTAMP | nullable |
| created_by | UUID | FK → users (self-ref) |
| created_at / updated_at | TIMESTAMP | auto |

### vendors
`id` PK · `user_id` UUID FK→users **1:1** · `email` · `company_name` VARCHAR(255) not null · `phone` VARCHAR(20) · `description` TEXT · `is_active` bool default true · `created_at`

### partners
`id` PK · `user_id` UUID FK→users **1:1** · `vendor_id` UUID FK→vendors (**N:1 Vendor**) · `email` · `company_name` not null · `phone` · `description` · `is_active` · `created_at`

### courses
`id` PK · `title` not null · `subtitle` · `description` TEXT not null · `category` not null · `created_at`/`updated_at`

### batches (index on course_id)
`id` PK · `course_id` UUID not null (logical FK) · `title` not null · `subtitle` · `description` not null · `starting_date` DATE · `ending_date` DATE · timestamps

### sessions (indexes: course_id, batch_id)
`id` PK · `course_id` · `batch_id` UUID not null · `session_type` enum not null · `title` not null · `subtitle` · `description` not null · `live_link`/`recorded_link`/`resource_link` VARCHAR(2083) · `sequence_order` INTEGER not null · `scheduled_date` DATE · `scheduled_time` TIME · `end_time` TIME (added by Flyway V1) · timestamps

### students (index: partner_id)
`id` PK · `partner_id` UUID not null · `first_name`/`last_name` VARCHAR(100) · `full_name` VARCHAR(205) (auto-computed @PrePersist/@PreUpdate) · `email` unique not null · `phone_number` · `college` · `branch` · `academic_year` · `is_deleted_by_partner` bool (soft delete) · timestamps

### student_enrollments (indexes: student/course/batch)
`id` PK · `student_id`/`course_id`/`batch_id` UUID · `course_name`/`batch_name` (snapshot) · `status` enum · `is_deleted_by_partner` bool · `enrolled_at` · `completed_at`

### student_session_logs (indexes: student/session/course/batch)
`id` PK · `student_id`/`session_id` · `session_type` enum · `course_id`+`course_name` · `batch_id`+`batch_name` · `entry_time` TIMESTAMP not null · `completion_time` · `time_spent_sec` INTEGER

### partner_access_requests (indexes: partner/vendor/course/batch)
`id` PK · `partner_id`/`vendor_id`/`course_id` UUID · `batch_id` nullable · `partner_name`/`course_name`/`batch_name` snapshots · `status` enum · `request_note`/`response_note` TEXT · `has_batch_access` BOOL · `max_students` INTEGER (seat limit) · `requested_at` · `resolved_at`

### student_complaints (indexes: student/partner/vendor/course/batch)
`id` PK · `student_id` nullable · `partner_id`/`vendor_id` · `course_id`/`batch_id` nullable · denormalized: `student_name`, `email`, `college`, `branch`, `academic_year`, `course_name`, `batch_name`, `partner_name`, `vendor_name`, `complaint_title` · `complaint_text` TEXT · `status` enum · `vendor_remark`/`partner_remark` TEXT · `is_resolved_by_vendor`/`is_resolved_by_partner` bool · timestamps

### user_sessions (login audit)
`id` PK · `user_id` FK→users (N:1) · `session_id` VARCHAR unique (Redis key/JTI) · `ip_address` · `browser`/`os`/`device_type`/`device`/`device_model`/`os_version`/`client_version` · `login_at` · `logout_at` · `is_active` bool

### audit_logs
`id` PK · `user_id` FK→users nullable · `action` VARCHAR(100) (e.g. PARTNER_ONBOARDED, VENDOR_BLOCKED) · `entity_name` · `entity_id` VARCHAR(36) · `ip_address` · `payload` TEXT (JSON) · `created_at`

### api_logs (per-request)
`id` PK · `username` · `role` · `session_id` (masked 8 chars) · `ip_address` · `api_url` · `method` · `request_body`/`response_body`/`error_message`/`success_message` TEXT · `status_code` · `is_error` · `execution_time_ms` · `created_at`

### notifications + notification_recipients (vendor→partners fan-out, effectively N:M)
- **notifications**: `id` PK · `vendor_id` FK→vendors · `title` not null · `message` TEXT · `target_type` enum · `target_entity_id` UUID · `priority` enum default NORMAL · timestamps
- **notification_recipients**: `id` PK · `notification_id` FK · `partner_id` FK · `is_read` bool · `read_at` · `delivered` bool · `delivered_at` · `created_at`

### Relationship summary
users 1:1 vendors, users 1:1 partners, vendors 1:N partners, users 1:N user_sessions/audit_logs, courses 1:N batches 1:N sessions (logical FKs), notifications N:M partners via notification_recipients.

---

## 7. Complete REST API Catalog

> Envelope for every response: `ApiResponse<T> { data, message, error, errorCode }`
> Public (no auth): `/auth/**`, `/open/**`, Swagger. Everything else needs the `session_id` cookie.

### /auth (public)
| Method | Path | Purpose |
|---|---|---|
| POST | `/auth/login` | `{username, password}` + device headers → sets HttpOnly `session_id` cookie; returns composite `{user, vendor, partner}` |
| GET | `/auth/session` | Validate cookie against Redis (used by frontend on every boot) |
| POST | `/auth/logout` | Delete Redis session, deactivate UserSession row, clear cookie |
| POST | `/auth/send-otp` | `{usernameOrEmail}` → 6-digit OTP emailed, `otp_session` cookie (30 min) |
| POST | `/auth/set-password` | `{otp, newPassword}` → account ACTIVE + emailVerified |
| POST | `/auth/forgot-username` | Email the username (anti-enumeration: always 200) |
| GET | `/auth/users` | SUPER_ADMIN only: list users |

### /vendors (SUPER_ADMIN)
POST/GET `/vendors` · GET/PUT/DELETE `/vendors/{id}` — vendor CRUD; DELETE removes vendor **and all its partners**.

### /partners (SUPER_ADMIN + VENDOR)
POST/GET `/partners` · GET `/partners/vendor/{vendorId}` · GET/PUT/DELETE `/partners/{id}` — ownership enforced; DELETE refused while partner has APPROVED access.

### /courses · /batches · /sessions (VENDOR writes, VENDOR+PARTNER reads)
- CRUD on `/courses`, `/batches?courseId=`, `/sessions?courseId&batchId&type&search&sortOrder`
- `PUT /sessions/reorder` — rewrite `sequenceOrder` (`{courseId, batchId, sessionIds:[{index,id}]}`)
- Session create auto-appends with `sequenceOrder = max+1`

### /partner-access (PARTNER)
| GET | `/partner-access/courses` | accessible courses (+ accessStatus) |
| GET | `/partner-access/batches?courseId=` | accessible batches |
| GET | `/partner-access/sessions?courseId&batchId` | sessions — live/recorded/resource links only if APPROVED |
| GET | `/partner-access/schedule?fromDate&toDate` | calendar grouped by day |
| GET | `/partner-access/students?batchId&sessionId` | partner's students |

### /access-requests
| POST | `/access-requests` | VENDOR grants directly (saved APPROVED) |
| POST | `/access-requests/request` | PARTNER requests (PENDING), with maxStudents + note |
| PUT | `/access-requests/{id}/status` | VENDOR approve/reject (+ retarget course/batch) |
| DELETE | `/access-requests/{id}` | delete |
| GET | `/access-requests/vendor/{vendorId}` · `/partner/{partnerId}` | listings |

### /partner-portal (VENDOR views a partner)
`/partner-portal/partner/{pid}/courses` · `.../courses/{cid}/batches` · `.../batches/{bid}/sessions` (with studentCount, maxStudents, limitExceeded) · `.../students` · `.../sessions/limit-crossed`

### /dashboard
`GET/PUT /dashboard/vendor/{vendorId}/profile` · `GET/PUT /dashboard/partner/{partnerId}/profile` · `GET /dashboard/vendor/{vendorId}` (counts + top-5 complaints) · `GET /dashboard/partner/{partnerId}` — ownership enforced.

### Notifications
| Method | Path | Role | Purpose |
|---|---|---|---|
| POST/GET/PUT/DELETE | `/vendors/notifications[/{id}]` | VENDOR | broadcast (targetType ALL_PARTNERS/COURSE/BATCH/SPECIFIC_PARTNERS, priority), history |
| GET | `/partners/notifications` | PARTNER | inbox (paged) |
| GET | `/partners/notifications/unread-count` | PARTNER | badge count |
| PATCH | `/partners/notifications/{id}/read` · `/read-all` | PARTNER | mark read |
| GET | `/partners/notifications/stream` | PARTNER | **SSE** live stream (`text/event-stream`) |

### /complaints (VENDOR + PARTNER)
POST `/complaints` · PUT/GET/DELETE `/complaints/{id}` · GET `/complaints?vendorId=` or `?partnerId=` — role-aware remarks (vendor vs partner).

### /open/sync (PUBLIC — used by student links)
| POST | `/open/sync/student-data` | idempotent upsert: Student + Enrollment + SessionLog in one call |
| POST | `/open/sync/complaints` | public complaint filing (Support Center) |
| GET | `/open/sync/courses?partnerId=` · `/open/sync/courses/{courseId}/batches?partnerId=` | open catalog |
| GET | `/open/sync/session-details?sessionId&partnerId` | Join Live Class page |

### /admin/master (SUPER_ADMIN — generic table admin, 12 resources)
For each of `batches, courses, partners, partner-access-requests, sessions, students, student-complaints, student-enrollments, student-session-logs, users, user-sessions, vendors`:
- `GET /admin/master/{resource}?page&size&search` — paged (Specification search)
- `DELETE /admin/master/{resource}/{id}` — delete one
- `DELETE /admin/master/{resource}` — delete **all rows**

### /admin/logs (API logs) & /admin/audit-logs (SUPER_ADMIN)
`GET ?page&size[&search]` · `DELETE /{id}` · `DELETE` (purge all).

---

## 8. Services

`AuthService` (login/session/OTP/logout) · `RedisService` (sessions `session:`, OTP `otp_session:`, rate limits `otp_count:`/`rate_limit:`) · `VendorService`/`PartnerService` (ownership-aware CRUD) · `Course/Batch/SessionService` (catalog + reorder) · `PartnerAccessService` (partner-scoped reads) · `PartnerAccessRequestService` (grant/request/approve workflow) · `PartnerPortalService` (vendor→partner views + seat limits) · `NotificationService` (fan-out) · `NotificationSender` (`@TransactionalEventListener(AFTER_COMMIT)` → SSE) · `StudentDataSyncService` (idempotent public upserts) · `StudentComplaintService` · `DashboardService` · `MasterDataService` (search+delete for admin tables) · `ApiLogService`/`AuditLogService` · `EmailService` (@Async) · `CookieService` · `HeartbeatService` (SSE ping every 30 s) · `JwtService` (**legacy**) · `AdminService`/`AdminSeeder`.

---

## 9. Security Model

1. **Auth = HttpOnly cookie + Redis sessions** (NOT JWT Bearer — `API_FLOW.md` is outdated).
   - Login → random UUID session stored in Redis `session:{id}` (TTL 3600 s) → HttpOnly `session_id` cookie (SameSite=Lax).
   - `JwtAuthenticationFilter` reads cookie → loads profile from Redis → populates SecurityContext.
2. **Roles:** `@PreAuthorize("hasAuthority('VENDOR')")` etc. (no ROLE_ prefix) + ownership checks in controllers/services.
3. **Account lifecycle:** created → `PENDING_SETUP` → OTP → set-password → `ACTIVE`; `BLOCKED` → force logout all sessions (`invalidateAllSessionsForUser`).
4. **Rate limiting:** 200 req/min/IP (HTTP 429), OTP 5/30 min in deployed envs.
5. **OTP:** Redis `otp_session:{token}` (30 min) + `otp_session` cookie; SecureRandom 6-digit; emailed via Hostinger SMTP.
6. **Public surface:** `/open/sync/**` is intentionally unauthenticated — security-sensitive.
7. CSRF disabled (cookie SameSite=Lax instead); CORS default; stateless session policy.
8. ⚠️ Plaintext secrets committed in `application.properties`, `.env.prod`, `.env.staging` (DB/Redis/SMTP passwords, JWT key, admin password). Cookies use `Secure=false` (TLS terminated at Traefik).

---

## 10. Cross-Cutting

- **SSE:** `SseConnectionManager` (partnerId → SseEmitter map, 1 h timeout, `connected` handshake); events pushed after transaction commit; 30 s heartbeat pings; filters bypass `/stream` endpoints.
- **Logging:** `MasterLoggingFilter` (MDC TraceID, verbose body logging) + `ApiLoggingFilter` (rate limit + async ApiLog persistence, single-thread executor queue 5000). Logback: console + conditional rolling `./logs/application.log`.
- **Scheduled:** SSE heartbeat (30 s). Async: email + log persistence.
- **No file uploads** — content is link-based (liveLink/recordedLink/resourceLink).
- **No WebSocket** — SSE only.

---

## 11. Deployment

- **Dockerfile:** multi-stage — `maven:3.9-eclipse-temurin-21-alpine` build → `eclipse-temurin:21-jre-alpine` runtime, non-root user, fat JAR.
- **build.sh:** builds image → `docker save` → gzip tarball (no registry).
- **deploy.sh:** scp zip (image + compose + env) to `root@200.97.175.94` → `docker compose down` → `docker load` → `up -d` → cleanup + remove old image. Gated by `AUTO_DEPLOY`.
- **Paths on VM:** `/opt/bleep/bleep-learnhub/{production,staging}/backend`.
- Postgres + Redis run **on the host VM** (not in compose) — `vmsetup.sh` provisions them; UFW restricts 5432/6380 to docker nets.
- **Containers:** `bleep-learnhub-backend-prod` (8080), `bleep-learnhub-backend-staging` (8081).

---

## 12. Known Caveats

1. `API_FLOW.md` describes JWT Bearer auth — actual code uses cookie+Redis sessions.
2. `projectInfo/Database_Schema.md` is stale (e.g. `uid` on students, scheduled_date on batches).
3. `/open/sync/**` unauthenticated by design — treat as security-sensitive.
4. Secrets committed in plaintext.
5. Dashboard `activeEnrollments` is a placeholder; `MasterDataController` returns raw JPA entities (lazy relations may serialize oddly); `JwtService` + OAuth2 starters are dormant.
6. `ddl-auto=update` in deployed envs (schema managed by Hibernate + Flyway deltas).
7. **Antigravity IDE shows FALSE errors** on every Lombok `@RequiredArgsConstructor` class (`config/`, `controller/`, `dto/`, `entity/`, `event/`, `security/`, `service/` all go red): *"The Java feature 'Flexible Constructor Bodies' is only available with source level 25 and above."* This is a toolchain incompatibility — the IDE's Eclipse (ECJ) compiler + its bundled Lombok 1.18.39 agent generate Java-25-style constructors while this project compiles at Java 21. **Verified:** `mvn compile` is clean (exit 0); builds, Docker and deploys are unaffected. Fix: update the IDE's Java extension; do NOT "fix" the source code.
8. Git: `pull.rebase=true` is set for this repo (Oct 19, 2026) — prevents the `fatal: Need to specify how to reconcile divergent branches` pull failure that blocked a push on Oct 19.