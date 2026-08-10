# Architecture

## Auth Flow
- **Session-based**: HttpOnly cookies (`session_id`, `otp_session`) — no JWT in response bodies.
- **Stateless validation**: Redis-backed session lookup. `JwtAuthenticationFilter` reads the cookie, validates via Redis.
- **OTP flow**: Rate-limited via Redis (`app.otp.max-requests`, `app.otp.time-frame-minutes`). Used for password setup/reset.
- **Session timeout**: Configurable via `app.session.timeout-seconds` (default 3600).

## Auth Endpoints
```
POST /auth/login              — authenticate, get session_id cookie
GET  /auth/session            — validate session cookie, return profile
POST /auth/logout             — invalidate session cookie
POST /auth/send-otp          — send OTP email, get otp_session cookie
POST /auth/set-password      — verify OTP, set password (setup or reset)
POST /auth/forgot-username   — email the username to registered address
```

## Role Hierarchy
- **SUPER_ADMIN** — Full platform access
- **VENDOR** — Manages courses, batches, sessions, partners, notifications
- **PARTNER** — Browses course store, enrolls in courses, views calendar

Role-based access via `@PreAuthorize` annotations on controller methods.

## SSE (Real-time Notifications)
- `SseConnectionManager` maintains persistent SSE connections for partner notification streaming.
- `PartnerNotificationStreamController` handles SSE endpoint.
- `NotificationCreatedEvent` — Spring application event fired when a notification is created, picked up by listeners.

## Device Tracking
Custom headers logged per request via `ApiLoggingFilter`:
- `Device-Ip`, `Device-Type`, `Device`, `Device-Model`
- `OS-Name`, `OS-Version`, `Client-Name`, `Client-Version`

## Audit & Logging
- `ApiLoggingFilter` — Logs every API request/response.
- `MasterLoggingFilter` — Master-level logging.
- `AuditLogService` — Business-level audit trail.

## Package Structure
```
com.bleep.learnhub/
  controller/       — REST controllers (18 controllers)
  service/          — Business logic services
  service/impl/     — Service implementations
  repository/       — Spring Data JPA repositories
  entity/           — JPA entities
  entity/enums/     — Enums (Role, AccountStatus)
  dto/              — DTOs (request/, response/)
  config/           — Spring config (SecurityConfig, MailConfig, filters)
  security/         — JWT service, cookie service, user details, auth filter
  sse/              — SSE connection manager
  event/            — Spring application events
  exception/        — Custom exceptions
  constants/        — CookieConstants, etc.
```

## Database Migrations (Flyway)
- **Flyway** manages all schema changes — runs automatically on startup before Hibernate.
- `spring.jpa.hibernate.ddl-auto=validate` — Hibernate only validates, never modifies schema.
- Migration files live in `src/main/resources/db/migration/` (e.g., `V1__add_end_time_to_sessions.sql`).
- `spring.flyway.baseline-on-migrate=true` — handles existing DBs that predate Flyway.
- Adding a new column/table: create a new `V<next>__<desc>.sql` file, deploy — Flyway runs it.

## Session Entity Fields
- `id` (UUID), `courseId`, `batchId`, `sessionType` (CLASS/NOTE/ASSIGNMENT/PROJECT)
- `title`, `subtitle`, `description`
- `liveLink`, `recordedLink`, `resourceLink` — link fields vary by session type (CLASS uses live+recorded, others use resource)
- `sequenceOrder` — ordering within a batch
- `scheduledDate` (LocalDate), `scheduledTime` (LocalTime), `endTime` (LocalTime) — when the live class ends
- `createdAt`, `updatedAt`

## Controllers
| Controller | Path | Purpose |
|-----------|------|---------|
| AuthController | `/auth` | Login, logout, session, OTP, password |
| VendorController | `/vendors` | Vendor CRUD (admin) |
| PartnerController | `/partners` | Partner CRUD (admin) |
| PartnerPortalController | `/partner-portal` | Partner self-service |
| PartnerAccessController | `/partner-access` | Partner access management |
| PartnerAccessRequestController | `/partner-access-requests` | Access request workflow |
| PartnerNotificationController | `/partner-notifications` | Notification CRUD |
| PartnerNotificationStreamController | `/partner-notifications/stream` | SSE stream |
| VendorNotificationController | `/vendor-notifications` | Vendor notification management |
| CourseController | `/courses` | Course CRUD |
| BatchController | `/batches` | Batch CRUD |
| SessionController | `/sessions` | Session CRUD |
| StudentComplaintController | `/student-complaints` | Complaint management |
| MasterDataController | `/master-data` | Admin master data endpoints |
| DashboardController | `/dashboard` | Dashboard statistics |
| AuditLogController | `/audit-logs` | Audit log queries |
| ApiLogController | `/api-logs` | API log queries |
| OpenDataSyncController | `/open-data-sync` | External data sync |
