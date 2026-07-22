# Bleep LearnHub — Database Schema Documentation

This document describes every database table (JPA entity) and enum used in the Bleep LearnHub Backend.  
For each table you will find its **purpose**, a full **column breakdown**, and notes on **indexes and relationships**.

---

## Table of Contents

1. [Enums](#enums)
2. [Users Table](#1-users-table)
3. [Vendors Table](#2-vendors-table)
4. [Partners Table](#3-partners-table)
5. [Courses Table](#4-courses-table)
6. [Batches Table](#5-batches-table)
7. [Sessions Table](#6-sessions-table)
8. [Students Table](#7-students-table)
9. [Student Enrollments Table](#8-student-enrollments-table)
10. [Student Session Logs Table](#9-student-session-logs-table)
11. [Partner Access Requests Table](#10-partner-access-requests-table)
12. [Student Complaints Table](#11-student-complaints-table)
13. [User Sessions Table](#12-user-sessions-table)
14. [Audit Logs Table](#13-audit-logs-table)
15. [Entity Relationship Overview](#entity-relationship-overview)

---

## Enums

Enums are stored as `STRING` values in the database (via `@Enumerated(EnumType.STRING)`).

### Role
**File:** `enums/Role.java`

| Value | Description |
|---|---|
| `SUPER_ADMIN` | Platform-level administrator with unrestricted access. |
| `VENDOR` | An organization that creates and delivers courses. |
| `PARTNER` | An organization (e.g. a college) that subscribes to a Vendor's courses for its students. |

**Used in:** `users.role`

---

### AccountStatus
**File:** `enums/AccountStatus.java`

| Value | Description |
|---|---|
| `PENDING_SETUP` | Account created but the user has not yet set a password / completed onboarding. |
| `ACTIVE` | Fully operational account. |
| `BLOCKED` | Account disabled by an administrator. |

**Used in:** `users.status`

---

### SessionType
**File:** `enums/SessionType.java`

| Value | Description |
|---|---|
| `CLASS` | A live or recorded class lecture. |
| `NOTE` | Study notes / reading material. |
| `ASSIGNMENT` | A homework or practice assignment. |
| `PROJECT` | A hands-on project or capstone task. |

**Used in:** `sessions.session_type`, `student_session_logs.session_type`

---

### EnrollmentStatus
**File:** `enums/EnrollmentStatus.java`

| Value | Description |
|---|---|
| `ENROLLED` | Student is actively enrolled. |
| `REMOVED` | Enrollment removed (e.g. by partner). |
| `DELETED` | Soft-deleted enrollment record. |

**Used in:** `student_enrollments.status`

---

### AccessRequestStatus
**File:** `enums/AccessRequestStatus.java`

| Value | Description |
|---|---|
| `PENDING` | Request has been submitted and is awaiting vendor review. |
| `APPROVED` | Vendor has granted access. |
| `REJECTED` | Vendor has denied the request. |

**Used in:** `partner_access_requests.status`

---

### ComplaintStatus
**File:** `enums/ComplaintStatus.java`

| Value | Description |
|---|---|
| `PENDING` | Complaint filed but not yet addressed. |
| `IN_PROGRESS` | Complaint is being actively investigated / resolved. |
| `RESOLVED` | Issue has been resolved. |

**Used in:** `student_complaints.status`

---

## 1. Users Table

**Table name:** `users`  
**Entity:** `User.java`

### Purpose
Central authentication and identity table. Every person who can log in to the platform (Super Admin, Vendor, or Partner) has exactly one row here. Students do **not** have user accounts — they are managed separately.

### Columns

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, Auto-generated | Unique identifier. |
| `username` | VARCHAR(100) | Unique, Not Null | Login username. |
| `email` | VARCHAR(255) | Unique, Not Null | Login email address. |
| `password_hash` | TEXT | Nullable | Bcrypt-hashed password. Null while account is in `PENDING_SETUP`. |
| `role` | ENUM (String) | Not Null | One of `SUPER_ADMIN`, `VENDOR`, `PARTNER`. |
| `status` | ENUM (String) | Not Null | One of `PENDING_SETUP`, `ACTIVE`, `BLOCKED`. |
| `email_verified` | BOOLEAN | Not Null, Default `false` | Whether the user's email has been verified. |
| `last_login_at` | TIMESTAMP | Nullable | Timestamp of the most recent successful login. |
| `created_by` | UUID (FK → `users.id`) | Nullable | Self-referencing FK — the admin/vendor who created this account. |
| `created_at` | TIMESTAMP | Auto-set, Not updatable | Row creation time. |
| `updated_at` | TIMESTAMP | Auto-updates | Last modification time. |

### Relationships
- **Self-referencing:** `created_by` → `users.id` (the user who created this account).
- **One-to-One:** Referenced by `Vendor.user` and `Partner.user`.
- **One-to-Many:** Referenced by `AuditLog.user` and `UserSession.user`.

---

## 2. Vendors Table

**Table name:** `vendors`  
**Entity:** `Vendor.java`

### Purpose
Stores the profile/organization details for users with the `VENDOR` role. A Vendor is the entity that **creates courses and batches** and delivers educational content on the platform.

### Columns

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, Auto-generated | Unique identifier. |
| `user_id` | UUID (FK → `users.id`) | Not Null, One-to-One | Link to the Vendor's login credentials. |
| `email` | VARCHAR(255) | Nullable | Business contact email (may differ from login email). |
| `company_name` | VARCHAR(255) | Not Null | Organization / company name. |
| `phone` | VARCHAR(20) | Nullable | Contact phone number. |
| `description` | TEXT | Nullable | About the vendor / company bio. |
| `is_active` | BOOLEAN | Not Null, Default `true` | Soft-active flag. |
| `created_at` | TIMESTAMP | Auto-set, Not updatable | Row creation time. |

### Relationships
- **One-to-One:** `user_id` → `users.id`.
- **One-to-Many:** Referenced by `Partner.vendor`.

---

## 3. Partners Table

**Table name:** `partners`  
**Entity:** `Partner.java`

### Purpose
Stores the profile/organization details for users with the `PARTNER` role. A Partner is typically a **college or institution** that subscribes to a Vendor's courses and enrolls its students.

### Columns

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, Auto-generated | Unique identifier. |
| `user_id` | UUID (FK → `users.id`) | Not Null, One-to-One | Link to the Partner's login credentials. |
| `vendor_id` | UUID (FK → `vendors.id`) | Not Null, Many-to-One | The Vendor this Partner is associated with. |
| `email` | VARCHAR(255) | Nullable | Business contact email. |
| `company_name` | VARCHAR(255) | Not Null | College / institution name. |
| `phone` | VARCHAR(20) | Nullable | Contact phone number. |
| `description` | TEXT | Nullable | About the partner organization. |
| `is_active` | BOOLEAN | Not Null, Default `true` | Soft-active flag. |
| `created_at` | TIMESTAMP | Auto-set, Not updatable | Row creation time. |

### Relationships
- **One-to-One:** `user_id` → `users.id`.
- **Many-to-One:** `vendor_id` → `vendors.id`.

---

## 4. Courses Table

**Table name:** `courses`  
**Entity:** `Course.java`

### Purpose
Represents an educational course created by a Vendor. A course is the **top-level container** for learning content and can have multiple batches.

### Columns

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, Auto-generated | Unique identifier. |
| `title` | VARCHAR(255) | Not Null | Course title (e.g. "Full-Stack Web Development"). |
| `subtitle` | VARCHAR(255) | Nullable | Optional tagline or short description. |
| `description` | TEXT | Not Null | Detailed course description. |
| `category` | VARCHAR(255) | Not Null | Course category (e.g. "Programming", "Data Science"). |
| `created_at` | TIMESTAMP | Auto-set, Not updatable | Row creation time. |
| `updated_at` | TIMESTAMP | Auto-updates | Last modification time. |

### Relationships
- **One-to-Many:** A course has many `Batch` records.
- **Referenced by:** `sessions.course_id`, `student_enrollments.course_id`, `student_session_logs.course_id`, `partner_access_requests.course_id`, `student_complaints.course_id`.

---

## 5. Batches Table

**Table name:** `batches`  
**Entity:** `Batch.java`

### Purpose
A batch is a **scheduled cohort** within a course. For example, "Java Bootcamp – July 2026 Batch". Each batch has its own start date/time and contains its own set of sessions and enrolled students.

### Columns

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, Auto-generated | Unique identifier. |
| `course_id` | UUID | Not Null | The course this batch belongs to. |
| `title` | VARCHAR(255) | Not Null | Batch name (e.g. "Batch 3 – Evening"). |
| `subtitle` | VARCHAR(255) | Nullable | Optional subtitle. |
| `description` | TEXT | Not Null | Detailed batch description. |
| `scheduled_date` | DATE | Not Null | The date the batch starts. |
| `scheduled_time` | TIME | Not Null | The time the batch starts. |
| `created_at` | TIMESTAMP | Auto-set, Not updatable | Row creation time. |
| `updated_at` | TIMESTAMP | Auto-updates | Last modification time. |

### Indexes
| Index Name | Column |
|---|---|
| `idx_batches_course_id` | `course_id` |

### Relationships
- **Many-to-One (logical):** `course_id` → `courses.id` (no FK constraint, indexed).
- **One-to-Many:** A batch has many `Session` records.

---

## 6. Sessions Table

**Table name:** `sessions`  
**Entity:** `Session.java`

### Purpose
Represents a single learning unit within a batch — a **class, note, assignment, or project**. Sessions are ordered via `sequence_order` and can optionally carry links to live classes, recordings, and resources.

### Columns

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, Auto-generated | Unique identifier. |
| `course_id` | UUID | Not Null | The course this session belongs to. |
| `batch_id` | UUID | Not Null | The batch this session belongs to. |
| `session_type` | ENUM (String) | Not Null | One of `CLASS`, `NOTE`, `ASSIGNMENT`, `PROJECT`. |
| `title` | VARCHAR(255) | Not Null | Session title. |
| `subtitle` | VARCHAR(255) | Nullable | Optional subtitle. |
| `description` | TEXT | Not Null | Detailed session description. |
| `live_link` | VARCHAR(2083) | Nullable | URL for a live class (e.g. Zoom, Google Meet). |
| `recorded_link` | VARCHAR(2083) | Nullable | URL for the recorded version. |
| `resource_link` | VARCHAR(2083) | Nullable | URL for downloadable resources. |
| `sequence_order` | INTEGER | Not Null | Display order within the batch. |
| `scheduled_date` | DATE | Nullable | Planned date for this session. |
| `scheduled_time` | TIME | Nullable | Planned time for this session. |
| `created_at` | TIMESTAMP | Auto-set, Not updatable | Row creation time. |
| `updated_at` | TIMESTAMP | Auto-updates | Last modification time. |

### Indexes
| Index Name | Column |
|---|---|
| `idx_sessions_course_id` | `course_id` |
| `idx_sessions_batch_id` | `batch_id` |

---

## 7. Students Table

**Table name:** `students`  
**Entity:** `Student.java`

### Purpose
Stores **student profiles** uploaded/created by Partners. Students do not log in to the admin system — they are end-learners whose data is managed by their Partner. Each student has a unique `uid` assigned by the partner.

### Columns

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, Auto-generated | Unique identifier. |
| `partner_id` | UUID | Not Null | The Partner who manages this student. |
| `uid` | VARCHAR(100) | Unique, Not Null | External unique ID assigned by the partner (e.g. roll number). |
| `first_name` | VARCHAR(100) | Not Null | Student's first name. |
| `last_name` | VARCHAR(100) | Not Null | Student's last name. |
| `full_name` | VARCHAR(205) | Not Null | Auto-computed from first + last name. |
| `email` | VARCHAR(255) | Unique, Not Null | Student's email. |
| `phone_number` | VARCHAR(20) | Nullable | Phone number. |
| `college` | VARCHAR(255) | Nullable | College / institution name. |
| `branch` | VARCHAR(100) | Nullable | Academic branch (e.g. "Computer Science"). |
| `academic_year` | VARCHAR(50) | Nullable | Year of study (e.g. "3rd Year"). |
| `is_deleted_by_partner` | BOOLEAN | Not Null, Default `false` | Soft-delete flag set by the partner. |
| `created_at` | TIMESTAMP | Auto-set, Not updatable | Row creation time. |
| `updated_at` | TIMESTAMP | Auto-updates | Last modification time. |

### Indexes
| Index Name | Column |
|---|---|
| `idx_students_partner_id` | `partner_id` |

### Special Behavior
- `full_name` is **automatically computed** via `@PrePersist` / `@PreUpdate` by concatenating `firstName` and `lastName`.

---

## 8. Student Enrollments Table

**Table name:** `student_enrollments`  
**Entity:** `StudentEnrollment.java`

### Purpose
Records the **many-to-many relationship** between students and batches. When a Partner enrolls a student in a specific course batch, a row is created here. This also tracks whether the enrollment is active, removed, or deleted.

### Columns

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, Auto-generated | Unique identifier. |
| `student_id` | UUID | Not Null | The enrolled student. |
| `course_id` | UUID | Not Null | The course the student is enrolled in. |
| `batch_id` | UUID | Not Null | The specific batch within the course. |
| `course_name` | VARCHAR(255) | Not Null | Denormalized course name (captured at enrollment time). |
| `batch_name` | VARCHAR(255) | Not Null | Denormalized batch name (captured at enrollment time). |
| `status` | ENUM (String) | Not Null | One of `ENROLLED`, `REMOVED`, `DELETED`. |
| `is_deleted_by_partner` | BOOLEAN | Not Null, Default `false` | Soft-delete flag. |
| `enrolled_at` | TIMESTAMP | Auto-set, Not updatable | When the enrollment was created. |
| `completed_at` | TIMESTAMP | Nullable | When the student completed the course. |

### Indexes
| Index Name | Column |
|---|---|
| `idx_se_student_id` | `student_id` |
| `idx_se_course_id` | `course_id` |
| `idx_se_batch_id` | `batch_id` |

### Design Notes
- `course_name` and `batch_name` are **denormalized** so that historical enrollment records remain readable even if the original course or batch is renamed.

---

## 9. Student Session Logs Table

**Table name:** `student_session_logs`  
**Entity:** `StudentSessionLog.java`

### Purpose
Tracks **individual student activity** within sessions. Every time a student opens a session, a log is created with their entry time. When they finish, the completion time and total time spent are recorded. This powers analytics dashboards for partners and vendors.

### Columns

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, Auto-generated | Unique identifier. |
| `student_id` | UUID | Not Null | The student who accessed the session. |
| `session_id` | UUID | Not Null | The session that was accessed. |
| `session_type` | ENUM (String) | Not Null | One of `CLASS`, `NOTE`, `ASSIGNMENT`, `PROJECT`. |
| `course_id` | UUID | Not Null | The course the session belongs to. |
| `course_name` | VARCHAR(255) | Not Null | Denormalized course name. |
| `batch_id` | UUID | Not Null | The batch the session belongs to. |
| `batch_name` | VARCHAR(255) | Not Null | Denormalized batch name. |
| `entry_time` | TIMESTAMP | Not Null | When the student opened the session. |
| `completion_time` | TIMESTAMP | Nullable | When the student finished the session. |
| `time_spent_sec` | INTEGER | Nullable | Total seconds spent in the session. |

### Indexes
| Index Name | Column |
|---|---|
| `idx_ssl_student_id` | `student_id` |
| `idx_ssl_session_id` | `session_id` |
| `idx_ssl_course_id` | `course_id` |
| `idx_ssl_batch_id` | `batch_id` |

---

## 10. Partner Access Requests Table

**Table name:** `partner_access_requests`  
**Entity:** `PartnerAccessRequest.java`

### Purpose
When a Partner wants to access a Vendor's course (and optionally a specific batch), they submit an **access request**. The Vendor can then approve or reject the request. This table acts as the workflow/ticketing mechanism for that process.

### Columns

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, Auto-generated | Unique identifier. |
| `partner_id` | UUID | Not Null | The Partner who submitted the request. |
| `vendor_id` | UUID | Not Null | The Vendor who owns the course. |
| `course_id` | UUID | Not Null | The course being requested. |
| `batch_id` | UUID | Nullable | Optionally, a specific batch within the course. |
| `partner_name` | VARCHAR(255) | Not Null | Denormalized partner name. |
| `course_name` | VARCHAR(255) | Not Null | Denormalized course name. |
| `batch_name` | VARCHAR(255) | Nullable | Denormalized batch name. |
| `status` | ENUM (String) | Not Null | One of `PENDING`, `APPROVED`, `REJECTED`. |
| `request_note` | TEXT | Nullable | Note from the Partner explaining the request. |
| `has_batch_access` | BOOLEAN | Nullable | Whether the request is for batch level access. |
| `max_students` | INTEGER | Nullable | Maximum allowed students for this access request. |
| `response_note` | TEXT | Nullable | Note from the Vendor with approval/rejection reason. |
| `requested_at` | TIMESTAMP | Auto-set, Not updatable | When the request was submitted. |
| `resolved_at` | TIMESTAMP | Nullable | When the Vendor approved or rejected the request. |

### Indexes
| Index Name | Column |
|---|---|
| `idx_par_partner_id` | `partner_id` |
| `idx_par_vendor_id` | `vendor_id` |
| `idx_par_course_id` | `course_id` |
| `idx_par_batch_id` | `batch_id` |

---

## 11. Student Complaints Table

**Table name:** `student_complaints`  
**Entity:** `StudentComplaint.java`

### Purpose
Stores **support tickets / complaints** raised by students. When a student faces an issue with a course or batch, a complaint is filed here. Both the Partner and the Vendor can add resolution remarks. The status tracks the lifecycle of the complaint from `PENDING` → `IN_PROGRESS` → `RESOLVED`.

### Columns

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, Auto-generated | Unique identifier. |
| `student_id` | UUID | Not Null | The student who raised the complaint. |
| `partner_id` | UUID | Not Null | The Partner managing the student. |
| `vendor_id` | UUID | Not Null | The Vendor delivering the course. |
| `course_id` | UUID | Not Null | The course the complaint is about. |
| `batch_id` | UUID | Not Null | The batch the complaint is about. |
| `student_name` | VARCHAR(255) | Not Null | Denormalized — captured at time of complaint. |
| `email` | VARCHAR(255) | Not Null | Denormalized student email. |
| `phone_number` | VARCHAR(20) | Nullable | Denormalized student phone. |
| `college` | VARCHAR(255) | Nullable | Denormalized college name. |
| `branch` | VARCHAR(100) | Nullable | Denormalized academic branch. |
| `academic_year` | VARCHAR(50) | Nullable | Denormalized year of study. |
| `course_name` | VARCHAR(255) | Not Null | Denormalized course name. |
| `batch_name` | VARCHAR(255) | Not Null | Denormalized batch name. |
| `complaint_title` | VARCHAR(255) | Not Null | Short subject line for the issue. |
| `complaint_text` | TEXT | Not Null | Full issue description written by the student. |
| `status` | ENUM (String) | Not Null | One of `PENDING`, `IN_PROGRESS`, `RESOLVED`. |
| `vendor_remark` | TEXT | Nullable | Notes/response added by the Vendor. |
| `partner_remark` | TEXT | Nullable | Notes/response added by the Partner. |
| `created_at` | TIMESTAMP | Auto-set, Not updatable | When the complaint was filed. |
| `updated_at` | TIMESTAMP | Auto-updates | Updated when status changes or remarks are added. |

### Indexes
| Index Name | Column |
|---|---|
| `idx_sc_student_id` | `student_id` |
| `idx_sc_partner_id` | `partner_id` |
| `idx_sc_vendor_id` | `vendor_id` |
| `idx_sc_course_id` | `course_id` |
| `idx_sc_batch_id` | `batch_id` |

### Design Notes
- All student/course/batch fields are **denormalized** (snapshot at complaint creation time) so that complaint records remain accurate even if the source data is later changed.
- UUID reference columns have **no foreign key constraints** — only indexes for query performance.

---

## 12. User Sessions Table

**Table name:** `user_sessions`  
**Entity:** `UserSession.java`

### Purpose
Tracks **active login sessions** for admin users (Vendors, Partners, Super Admins). Every time a user logs in, a new session record is created containing the JWT identifier (JTI) and device metadata. This enables multi-device session management, forced logout, and login history.

### Columns

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, Auto-generated | Unique identifier. |
| `user_id` | UUID (FK → `users.id`) | Not Null | The logged-in user. |
| `session_id` | VARCHAR | Unique, Not Null | The JTI (JWT ID) claim of the issued token. |
| `ip_address` | VARCHAR(45) | Nullable | IP address of the client (supports IPv6). |
| `browser` | VARCHAR | Nullable | Browser name (e.g. "Chrome"). |
| `os` | VARCHAR | Nullable | Operating system (e.g. "Windows 11"). |
| `device_type` | VARCHAR | Nullable | Device category (e.g. "Desktop", "Mobile"). |
| `device` | VARCHAR | Nullable | Device brand/name. |
| `device_model` | VARCHAR | Nullable | Specific device model. |
| `os_version` | VARCHAR | Nullable | OS version string. |
| `client_version` | VARCHAR | Nullable | Frontend app version. |
| `login_at` | TIMESTAMP | Auto-set, Not updatable | When the session started. |
| `logout_at` | TIMESTAMP | Nullable | When the session ended (null = still active). |
| `is_active` | BOOLEAN | Not Null, Default `true` | Whether the session is currently active. |

### Relationships
- **Many-to-One:** `user_id` → `users.id`.

---

## 13. Audit Logs Table

**Table name:** `audit_logs`  
**Entity:** `AuditLog.java`

### Purpose
An **append-only** event log that records significant actions across the platform for compliance, debugging, and security auditing. Examples include partner onboarding, vendor blocking, password resets, etc.

### Columns

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, Auto-generated | Unique identifier. |
| `user_id` | UUID (FK → `users.id`) | Nullable | The user who performed the action. Null for unauthenticated events. |
| `action` | VARCHAR(100) | Not Null | Action identifier (e.g. `PARTNER_ONBOARDED`, `VENDOR_BLOCKED`, `PASSWORD_RESET`). |
| `entity_name` | VARCHAR(100) | Nullable | The type of entity affected (e.g. "Partner", "Vendor"). |
| `entity_id` | VARCHAR(36) | Nullable | The ID of the affected entity (stored as String for flexibility). |
| `ip_address` | VARCHAR(45) | Nullable | IP address of the requester. |
| `payload` | TEXT | Nullable | Serialized JSON snapshot of the request that triggered the event. |
| `created_at` | TIMESTAMP | Auto-set, Not updatable | When the event occurred. |

### Relationships
- **Many-to-One:** `user_id` → `users.id` (nullable — some events occur before authentication).

---

## Entity Relationship Overview

```
┌──────────┐     1:1      ┌──────────┐
│  Users   │◄────────────►│ Vendors  │
│          │              └────┬─────┘
│          │     1:1           │ 1:N
│          │◄──────────┐  ┌───┴──────┐
└────┬─────┘           │  │ Partners │
     │                 │  └───┬──────┘
     │ 1:N             │      │
┌────┴──────────┐      │      │ 1:N
│ User Sessions │      │  ┌───┴──────┐
└───────────────┘      │  │ Students │
                       │  └───┬──────┘
┌───────────────┐      │      │ 1:N
│  Audit Logs   │──────┘  ┌───┴──────────────────┐
└───────────────┘         │ Student Enrollments   │
                          └──────────────────────┘
┌──────────┐   1:N   ┌──────────┐   1:N   ┌───────────┐
│ Courses  │────────►│ Batches  │────────►│ Sessions  │
└──────────┘         └──────────┘         └─────┬─────┘
                                                │ 1:N
                                    ┌───────────┴──────────┐
                                    │ Student Session Logs  │
                                    └──────────────────────┘

┌───────────────────────────┐     ┌────────────────────────┐
│ Partner Access Requests   │     │  Student Complaints    │
│ (Partner ↔ Vendor workflow│     │ (Student support       │
│  for course access)       │     │  tickets)              │
└───────────────────────────┘     └────────────────────────┘
```

### Key Design Decisions

1. **No Foreign Keys on denormalized/cross-reference tables:** Tables like `student_enrollments`, `student_session_logs`, `partner_access_requests`, and `student_complaints` use UUID columns with **indexes but no database-level foreign key constraints**. This keeps the schema loosely coupled and avoids cascading delete issues across service boundaries.

2. **Denormalized fields:** Many tables snapshot names (student name, course name, batch name, etc.) at the time of record creation. This ensures historical records remain accurate even if the source entity is later renamed or deleted.

3. **Soft deletes over hard deletes:** Flags like `is_deleted_by_partner` and `is_active` are used instead of physically deleting rows, preserving data for auditing and analytics.

4. **UUID everywhere:** All primary keys are auto-generated UUIDs, making records globally unique and safe for distributed systems.
