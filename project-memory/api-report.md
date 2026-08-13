# Bleep LearnHub - Complete API Report

Generated: 2026-08-13
Source: Backend controllers + Frontend hook cross-reference

---

## Architecture Overview

- **Backend**: Spring Boot (Java), REST API, Redis sessions, PostgreSQL
- **Frontend**: React 19 + TypeScript + Vite, React Query hooks
- **Auth**: HttpOnly session cookies (no JWT in body), Redis-backed sessions
- **Base path**: `/api/v1` (configurable via `VITE_API_CONTEXT_PATH`)
- **Roles**: `SUPER_ADMIN`, `VENDOR`, `PARTNER`

---

## 1. Authentication (`/auth`)

Controller: `AuthController.java`

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/auth/login` | None | Login with username+password. Sets `session_id` HttpOnly cookie. Returns user profile + vendor/partner data based on role. |
| GET | `/auth/session` | None (cookie) | Validates `session_id` cookie from Redis. Returns same profile as login. |
| POST | `/auth/logout` | None (cookie) | Deletes Redis session, clears `session_id` cookie. |
| POST | `/auth/send-otp` | None | Sends 6-digit OTP to registered email. Sets `otp_session` cookie. Rate-limited: 3 per 30 min. |
| POST | `/auth/set-password` | None (otp cookie) | Validates OTP from `otp_session` cookie, sets new password. Clears OTP cookie on success. |
| POST | `/auth/forgot-username` | None | Emails username to registered address. Always returns 200 (anti-enumeration). |
| GET | `/auth/users` or `/auth/user-list` | SUPER_ADMIN | Lists all users. |

**Frontend hooks**: `src/hooks/AuthApi/hooks/Auth.hook.ts`

---

## 2. Courses (`/courses`)

Controller: `CourseController.java`

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/courses` | VENDOR | Create a new course. |
| PUT | `/courses/{id}` | VENDOR | Update course details. |
| DELETE | `/courses/{id}` | VENDOR | Delete a course. |
| GET | `/courses` | VENDOR, PARTNER | List all courses. |
| GET | `/courses/{id}` | VENDOR, PARTNER | Get course by ID with full profile. |

**Frontend hooks**: `src/hooks/CourseApi/hooks/Course.hook.ts`

---

## 3. Batches (`/batches`)

Controller: `BatchController.java`

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/batches` | VENDOR | Create a new batch. |
| PUT | `/batches/{id}` | VENDOR | Update batch details. |
| DELETE | `/batches/{id}` | VENDOR | Delete a batch. |
| GET | `/batches` | VENDOR, PARTNER | List all batches. Optional `courseId` query param to filter. |
| GET | `/batches/{id}` | VENDOR, PARTNER | Get batch by ID with full profile. |

**Frontend hooks**: `src/hooks/BatchApi/hooks/Batch.hook.ts`

---

## 4. Sessions (`/sessions`)

Controller: `SessionController.java`

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/sessions` | VENDOR | Create a new session (CLASS, NOTE, ASSIGNMENT, PROJECT). |
| PUT | `/sessions/{id}` | VENDOR | Update session details. |
| DELETE | `/sessions/{id}` | VENDOR | Delete a session. |
| GET | `/sessions` | VENDOR, PARTNER | List sessions with filters: `courseId`, `batchId`, `type`, `search`, `sortOrder`. |
| GET | `/sessions/{id}` | VENDOR, PARTNER | Get session by ID with full profile. |
| PUT | `/sessions/reorder` | VENDOR | Reorder sessions within a batch (drag-and-drop). |

**Frontend hooks**: `src/hooks/SessionApi/hooks/Session.hook.ts`

---

## 5. Vendors (`/vendors`)

Controller: `VendorController.java`

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/vendors` | SUPER_ADMIN | Create a new vendor. Sends onboarding email. |
| GET | `/vendors` | SUPER_ADMIN | List all vendors. |
| GET | `/vendors/{id}` | SUPER_ADMIN | Get vendor by ID. |
| PUT | `/vendors/{id}` | SUPER_ADMIN | Update vendor details. |
| DELETE | `/vendors/{id}` | SUPER_ADMIN | Delete vendor and all associated partners. |

**Frontend hooks**: `src/hooks/VendorApi/hooks/Vendor.hook.ts`

---

## 6. Partners (`/partners`)

Controller: `PartnerController.java`

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/partners` | SUPER_ADMIN, VENDOR | Create a new partner. Sends onboarding email. |
| GET | `/partners` | SUPER_ADMIN, VENDOR | List all partners (VENDOR sees only their own). |
| GET | `/partners/vendor/{vendorId}` | SUPER_ADMIN, VENDOR | List partners belonging to a vendor. |
| GET | `/partners/{id}` | SUPER_ADMIN, VENDOR | Get partner by ID. |
| PUT | `/partners/{id}` | SUPER_ADMIN, VENDOR | Update partner details. |
| DELETE | `/partners/{id}` | SUPER_ADMIN, VENDOR | Delete a partner. |

**Frontend hooks**: `src/hooks/PartnerApi/hooks/Partner.hook.ts`

---

## 7. Dashboard (`/dashboard`)

Controller: `DashboardController.java`

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| GET | `/dashboard/vendor/{vendorId}` | SUPER_ADMIN, VENDOR | Vendor dashboard: total courses, batches, partners, students, pending access requests, recent complaints. |
| GET | `/dashboard/partner/{partnerId}` | SUPER_ADMIN, PARTNER | Partner dashboard: vendor details, total students, active enrollments, recent complaints. |
| GET | `/dashboard/vendor/{vendorId}/profile` | VENDOR | Get vendor's own profile. |
| PUT | `/dashboard/vendor/{vendorId}/profile` | VENDOR | Update vendor's own profile. |
| GET | `/dashboard/partner/{partnerId}/profile` | PARTNER | Get partner's own profile. |
| PUT | `/dashboard/partner/{partnerId}/profile` | PARTNER | Update partner's own profile. |

**Frontend hooks**: `src/hooks/DashboardApi/hooks/Dashboard.hook.ts`

---

## 8. Partner Portal (Vendor View) (`/partner-portal`)

Controller: `PartnerPortalController.java`

Used by VENDORs to view partner-specific data.

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| GET | `/partner-portal/partner/{partnerId}/courses` | VENDOR | Get courses accessible to a partner. |
| GET | `/partner-portal/partner/{partnerId}/courses/{courseId}/batches` | VENDOR | Get batches for a partner's course. |
| GET | `/partner-portal/partner/{partnerId}/batches/{batchId}/sessions` | VENDOR | Get sessions for a partner's batch (includes studentCount, maxStudents, limitExceeded). |
| GET | `/partner-portal/partner/{partnerId}/students` | VENDOR | Get students enrolled by a partner. |
| GET | `/partner-portal/partner/{partnerId}/sessions/limit-crossed` | VENDOR | Get sessions where student count exceeds maxStudents limit. |

**Frontend hooks**: `src/hooks/PartnerPortalApi/hooks/PartnerPortal.hook.ts`

---

## 9. Partner Access (Partner View) (`/partner-access`)

Controller: `PartnerAccessController.java`

Used by PARTNERs to browse courses, batches, sessions, and their students.

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| GET | `/partner-access/courses` | PARTNER | Get courses the partner has access to (includes access status). |
| GET | `/partner-access/batches` | PARTNER | Get batches for a course (includes access status). Query: `courseId`, `partnerId`. |
| GET | `/partner-access/sessions` | PARTNER | Get sessions for a course+batch (includes access status). Query: `courseId`, `batchId`, `partnerId`. |
| GET | `/partner-access/schedule` | PARTNER | Get calendar schedule for a date range. Query: `fromDate`, `toDate`, `partnerId`. |
| GET | `/partner-access/students` | PARTNER | Get students enrolled by this partner. Optional filters: `partnerId`, `batchId`, `sessionId`. |

**Frontend hooks**: `src/hooks/PartnerAccessApi/hooks/PartnerAccess.hook.ts`

---

## 10. Access Requests (`/access-requests`)

Controller: `PartnerAccessRequestController.java`

Manages the access request workflow between VENDORs and PARTNERs.

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/access-requests` | VENDOR | Vendor creates an access request for a partner. |
| POST | `/access-requests/request` | PARTNER | Partner requests access to a course/batch. |
| PUT | `/access-requests/{id}/status` | VENDOR | Vendor approves/rejects an access request. |
| DELETE | `/access-requests/{id}` | VENDOR | Delete an access record. |
| GET | `/access-requests/vendor/{vendorId}` | VENDOR, SUPER_ADMIN | Get all access requests for a vendor. |
| GET | `/access-requests/partner/{partnerId}` | VENDOR, SUPER_ADMIN | Get all access requests made by a partner. |

**Frontend hooks**: `src/hooks/PartnerAccessRequestApi/hooks/PartnerAccessRequest.hook.ts`

---

## 11. Student Complaints (`/complaints`)

Controller: `StudentComplaintController.java`

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/complaints` | VENDOR, PARTNER | Submit a new student complaint. |
| PUT | `/complaints/{id}` | VENDOR, PARTNER | Update complaint (status, vendorRemark, partnerRemark). |
| DELETE | `/complaints/{id}` | VENDOR, PARTNER | Delete a complaint. |
| GET | `/complaints` | VENDOR, PARTNER | List complaints. Requires `vendorId` or `partnerId` query param. |
| GET | `/complaints/{id}` | VENDOR, PARTNER | Get complaint by ID. |

**Frontend hooks**: `src/hooks/StudentComplaintApi/hooks/StudentComplaint.hook.ts`

---

## 12. Vendor Notifications (`/vendors/notifications`)

Controller: `VendorNotificationController.java`

VENDORs create and manage notifications sent to PARTNERs.

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/vendors/notifications` | VENDOR | Create and broadcast a notification to partners. Target types: ALL_PARTNERS, COURSE, BATCH, SPECIFIC_PARTNERS. |
| GET | `/vendors/notifications` | VENDOR | Get vendor's sent notification history (paginated). |
| PUT | `/vendors/notifications/{id}` | VENDOR | Edit a sent notification (title, message, priority). |
| DELETE | `/vendors/notifications/{id}` | VENDOR | Delete a notification. |

**Frontend hooks**: `src/hooks/VendorNotificationApi/hooks/VendorNotification.hook.ts`

---

## 13. Partner Notifications (`/partners/notifications`)

Controller: `PartnerNotificationController.java`

PARTNERs read and manage received notifications.

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| GET | `/partners/notifications` | PARTNER | Get partner's notification inbox (paginated). |
| GET | `/partners/notifications/unread-count` | PARTNER | Get count of unread notifications (bell badge). |
| PATCH | `/partners/notifications/{id}/read` | PARTNER | Mark a single notification as read. |
| PATCH | `/partners/notifications/read-all` | PARTNER | Mark all notifications as read. |

**Frontend hooks**: `src/hooks/PartnerNotificationApi/hooks/PartnerNotification.hook.ts`

---

## 14. Partner Notification SSE Stream (`/partners/notifications/stream`)

Controller: `PartnerNotificationStreamController.java`

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| GET | `/partners/notifications/stream` | PARTNER | Server-Sent Events stream for real-time notification delivery. |

**Frontend hooks**: `src/hooks/PartnerNotificationApi/hooks/PartnerNotificationStream.hook.ts`

---

## 15. Open Data Sync (`/open/sync`)

Controller: `OpenDataSyncController.java`

**No authentication required** - used by external partner systems to sync data.

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/open/sync/student-data` | None | Sync student data (creates/updates Student, Enrollment, SessionLog in one call). |
| POST | `/open/sync/complaints` | None | Submit a complaint from external partner system. |
| GET | `/open/sync/courses` | None | Get courses for a partner. Query: `partnerId`. |
| GET | `/open/sync/courses/{courseId}/batches` | None | Get batches for a partner's course. Query: `partnerId`. |
| GET | `/open/sync/session-details` | None | Get session details for joining a live class. Query: `sessionId`, `partnerId`. |

**Frontend hooks**: `src/hooks/OpenDataSyncApi/hooks/OpenDataSync.hook.ts`

---

## 16. Admin Master Data (`/admin/master`)

Controller: `MasterDataController.java`

SUPER_ADMIN only. Full CRUD (GET paginated + DELETE single/all) for all entities.

| Entity | GET Endpoint | DELETE Endpoint |
|--------|-------------|-----------------|
| Batches | `/admin/master/batches` | `/admin/master/batches/{id}`, `/admin/master/batches` |
| Courses | `/admin/master/courses` | `/admin/master/courses/{id}`, `/admin/master/courses` |
| Partners | `/admin/master/partners` | `/admin/master/partners/{id}`, `/admin/master/partners` |
| PartnerAccessRequests | `/admin/master/partner-access-requests` | `/admin/master/partner-access-requests/{id}`, `/admin/master/partner-access-requests` |
| Sessions | `/admin/master/sessions` | `/admin/master/sessions/{id}`, `/admin/master/sessions` |
| Students | `/admin/master/students` | `/admin/master/students/{id}`, `/admin/master/students` |
| StudentComplaints | `/admin/master/student-complaints` | `/admin/master/student-complaints/{id}`, `/admin/master/student-complaints` |
| StudentEnrollments | `/admin/master/student-enrollments` | `/admin/master/student-enrollments/{id}`, `/admin/master/student-enrollments` |
| Users | `/admin/master/users` | `/admin/master/users/{id}`, `/admin/master/users` |
| UserSessions | `/admin/master/user-sessions` | `/admin/master/user-sessions/{id}`, `/admin/master/user-sessions` |
| Vendors | `/admin/master/vendors` | `/admin/master/vendors/{id}`, `/admin/master/vendors` |
| StudentSessionLogs | `/admin/master/student-session-logs` | `/admin/master/student-session-logs/{id}`, `/admin/master/student-session-logs` |

All GET endpoints support: `page` (default 0), `size` (default 25), `search` (optional). Sorted by `createdAt` DESC.

**Frontend hooks**: `src/hooks/MasterApi/hooks/Admin*.hook.ts` (12 separate hook files)

---

## 17. Audit Logs (`/admin/audit-logs`)

Controller: `AuditLogController.java`

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| GET | `/admin/audit-logs` | SUPER_ADMIN | Get paginated audit logs. Query: `page`, `size`, `search`. |
| DELETE | `/admin/audit-logs/{id}` | SUPER_ADMIN | Delete a single audit log. |
| DELETE | `/admin/audit-logs` | SUPER_ADMIN | Delete all audit logs. |

**Frontend hooks**: `src/hooks/AuditLogApi/hooks/AuditLog.hook.ts`

---

## 18. API Logs (`/admin/logs`)

Controller: `ApiLogController.java`

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| GET | `/admin/logs` | SUPER_ADMIN | Get paginated API request logs. Query: `page`, `size` (default 100). |
| DELETE | `/admin/logs/{id}` | SUPER_ADMIN | Delete a single API log. |
| DELETE | `/admin/logs` | SUPER_ADMIN | Delete all API logs. |

**Frontend hooks**: `src/hooks/ApiLogApi/hooks/ApiLog.hook.ts`

---

## Summary Statistics

| Category | Endpoints |
|----------|-----------|
| Auth | 7 |
| Courses | 5 |
| Batches | 5 |
| Sessions | 6 |
| Vendors | 5 |
| Partners | 6 |
| Dashboard | 6 |
| Partner Portal (Vendor) | 5 |
| Partner Access (Partner) | 5 |
| Access Requests | 6 |
| Student Complaints | 5 |
| Vendor Notifications | 4 |
| Partner Notifications | 4 |
| Partner SSE Stream | 1 |
| Open Data Sync | 5 |
| Admin Master Data | 24 (12 GET + 12 DELETE) |
| Audit Logs | 3 |
| API Logs | 3 |
| **Total** | **105** |

### Role-Based Access Summary

| Role | Controllers Accessible |
|------|----------------------|
| **SUPER_ADMIN** | Auth (user list), Vendors, Partners, Dashboard (all), Access Requests, Master Data (all), Audit Logs, API Logs |
| **VENDOR** | Courses, Batches, Sessions, Partners, Dashboard (vendor), Partner Portal, Access Requests, Complaints, Vendor Notifications |
| **PARTNER** | Courses (read), Batches (read), Sessions (read), Dashboard (partner), Partner Access, Access Requests (create), Complaints, Partner Notifications, SSE Stream |
| **None (Open)** | Auth (login/otp/password), Open Data Sync |
