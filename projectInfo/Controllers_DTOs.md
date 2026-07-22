Created At: 2026-07-08T06:26:53Z
Completed At: 2026-07-08T06:26:54Z
File Path: `file:///c:/Users/Acer/OneDrive/Desktop/my_work/Bleep-LearnHub-Backend/projectInfo/Controllers_DTOs.md`

# Bleep LearnHub — Controllers & DTOs Documentation

This document provides a detailed explanation of every file in the `controller/` and `dto/` packages — what each file does, why it exists, and what it contains.

---

## Table of Contents

**Controllers (`controller/`)**
1. [AuthController.java](#1-authcontrollerjava)
2. [VendorController.java](#2-vendorcontrollerjava)
3. [PartnerController.java](#3-partnercontrollerjava)
4. [CourseController.java](#4-coursecontrollerjava)
5. [BatchController.java](#5-batchcontrollerjava)
6. [SessionController.java](#6-sessioncontrollerjava)
7. [PartnerAccessRequestController.java](#7-partneraccessrequestcontrollerjava)
8. [StudentComplaintController.java](#8-studentcomplaintcontrollerjava)

**DTOs — Root (`dto/`)**
9. [OtpSessionData.java](#9-otpsessiondatajava)

**DTOs — Request (`dto/request/`)**
10. [LoginRequestDto.java](#10-loginrequestdtojava)
11. [SendOtpRequestDto.java](#11-sendotprequestdtojava)
12. [SetPasswordRequestDto.java](#12-setpasswordrequestdtojava)
13. [SetPasswordDto.java](#13-setpassworddtojava-legacy)
14. [ForgotUsernameRequestDto.java](#14-forgotusernamerequestdtojava)
15. [VendorCreateDto.java](#15-vendorcreatedtojava)
16. [VendorUpdateDto.java](#16-vendorupdatedtojava)
17. [PartnerCreateDto.java](#17-partnercreatedtojava)
18. [PartnerUpdateDto.java](#18-partnerupdatedtojava)
19. [CourseCreateDto.java](#19-coursecreatedtojava)
20. [CourseUpdateDto.java](#20-courseupdatedtojava)
21. [BatchCreateDto.java](#21-batchcreatedtojava)
22. [BatchUpdateDto.java](#22-batchupdatedtojava)
23. [SessionCreateDto.java](#23-sessioncreatedtojava)
24. [SessionUpdateDto.java](#24-sessionupdatedtojava)
25. [AccessRequestDto.java](#25-accessrequestdtojava)
26. [AccessStatusUpdateDto.java](#26-accessstatusupdatedtojava)
27. [ComplaintCreateDto.java](#27-complaintcreatedtojava)
28. [ComplaintUpdateDto.java](#28-complaintupdatedtojava)

**DTOs — Response (`dto/response/`)**
29. [ApiResponse.java](#29-apiresponsejava)
30. [LoginResponseDto.java](#30-loginresponsedtojava)
31. [UserDataDto.java](#31-userdatadtojava)
32. [VendorDataDto.java](#32-vendordatadtojava)
33. [PartnerDataDto.java](#33-partnerdatadtojava)
34. [DeviceDetailsDto.java](#34-devicedetailsdtojava)
35. [AuthResponseDto.java](#35-authresponsedtojava-legacy)
36. [VendorProfileResponseDto.java](#36-vendorprofileresponsedtojava)
37. [PartnerProfileResponseDto.java](#37-partnerprofileresponsedtojava)
38. [CourseDataDto.java](#38-coursedatadtojava)
39. [CourseProfileResponseDto.java](#39-courseprofileresponsedtojava)
40. [BatchDataDto.java](#40-batchdatadtojava)
41. [BatchProfileResponseDto.java](#41-batchprofileresponsedtojava)
42. [SessionDataDto.java](#42-sessiondatadtojava)
43. [SessionProfileResponseDto.java](#43-sessionprofileresponsedtojava)
44. [AccessRequestResponseDto.java](#44-accessrequestresponsedtojava)
45. [ComplaintResponseDto.java](#45-complaintresponsedtojava)

---

# Controllers (`controller/`)

Controllers are the HTTP entry points of the application. They receive REST requests, validate input, delegate to services, and return standardized `ApiResponse<T>` responses. Controllers are intentionally **thin** — all business logic lives in services.

---

## 1. AuthController.java

**File:** `controller/AuthController.java`  
**Path:** `/auth/**` (all endpoints are **public** — no authentication required)

### Purpose
Handles the entire authentication lifecycle: login, session validation, logout, OTP management, password setup/reset, and forgot-username.

### Endpoints

| HTTP Method | Path | Purpose | Request Body | Response Body | Cookies |
|---|---|---|---|---|---|
| `POST` | `/auth/login` | Authenticate user | `LoginRequestDto` | `ApiResponse<LoginResponseDto>` | **Sets** `session_id` (7 days) |
| `GET` | `/auth/session` | Validate active session | — | `ApiResponse<LoginResponseDto>` | **Reads** `session_id` |
| `POST` | `/auth/logout` | End session | — | `ApiResponse<Void>` | **Clears** `session_id` |
| `POST` | `/auth/send-otp` | Request OTP email | `SendOtpRequestDto` | `ApiResponse<Void>` | **Sets** `otp_session` (30 min) |
| `POST` | `/auth/set-password` | Set/reset password with OTP | `SetPasswordRequestDto` | `ApiResponse<Void>` | **Reads + Clears** `otp_session` |
| `POST` | `/auth/forgot-username` | Email username to user | `ForgotUsernameRequestDto` | `ApiResponse<Void>` | — |
| `GET` | `/auth/users` or `/auth/user-list` | List all users | — | `ApiResponse<List<UserDataDto>>` | — |

---

## 2. VendorController.java

**File:** `controller/VendorController.java`  
**Path:** `/vendors/**`  
**Access:** `@PreAuthorize("hasAuthority('SUPER_ADMIN')")` — **Super Admin only**

### Purpose
CRUD endpoints for managing Vendor organizations. Only the Super Admin can create, view, update, or delete vendors.

### Endpoints

| HTTP Method | Path | Purpose | Request Body | Response Body |
|---|---|---|---|---|
| `POST` | `/vendors` | Create a new vendor | `VendorCreateDto` | `ApiResponse<Void>` |
| `GET` | `/vendors` | List all vendors | — | `ApiResponse<List<VendorProfileResponseDto>>` |
| `GET` | `/vendors/{id}` | Get vendor by ID | — | `ApiResponse<VendorProfileResponseDto>` |
| `PUT` | `/vendors/{id}` | Update vendor profile | `VendorUpdateDto` | `ApiResponse<Void>` |
| `DELETE` | `/vendors/{id}` | Delete vendor + all partners | — | `ApiResponse<Void>` |

---

## 3. PartnerController.java

**File:** `controller/PartnerController.java`  
**Path:** `/partners/**`  
**Access:** `@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'VENDOR')")` — **Super Admin or Vendor**

### Purpose
CRUD endpoints for managing Partner organizations. Vendors can manage their own partners; Super Admins can manage all partners.

### Endpoints

| HTTP Method | Path | Purpose | Request Body | Response Body |
|---|---|---|---|---|
| `POST` | `/partners` | Create a new partner | `PartnerCreateDto` | `ApiResponse<Void>` |
| `GET` | `/partners` | List partners | — | `ApiResponse<List<PartnerProfileResponseDto>>` |
| `GET` | `/partners/vendor/{vendorId}` | List partners by vendor | — | `ApiResponse<List<PartnerProfileResponseDto>>` |
| `GET` | `/partners/{id}` | Get partner by ID | — | `ApiResponse<PartnerProfileResponseDto>` |
| `PUT` | `/partners/{id}` | Update partner profile | `PartnerUpdateDto` | `ApiResponse<Void>` |
| `DELETE` | `/partners/{id}` | Delete partner | — | `ApiResponse<Void>` |

---

## 4. CourseController.java

**File:** `controller/CourseController.java`  
**Path:** `/courses/**`  
**Access:** Access varies by endpoint.

### Purpose
CRUD endpoints for managing courses. Vendors can create, update, and delete courses. Vendors and Partners can fetch courses.

### Endpoints

| HTTP Method | Path | Purpose | Request Body | Response Body | Access |
|---|---|---|---|---|---|
| `POST` | `/courses` | Create a new course | `CourseCreateDto` | `ApiResponse<CourseProfileResponseDto>` | `VENDOR` |
| `PUT` | `/courses/{id}` | Update course | `CourseUpdateDto` | `ApiResponse<Void>` | `VENDOR` |
| `DELETE` | `/courses/{id}` | Delete course | — | `ApiResponse<Void>` | `VENDOR` |
| `GET` | `/courses` | List all courses | — | `ApiResponse<List<CourseDataDto>>` | `VENDOR` or `PARTNER` |
| `GET` | `/courses/{id}` | Get course by ID | — | `ApiResponse<CourseProfileResponseDto>` | `VENDOR` or `PARTNER` |

---

## 5. BatchController.java

**File:** `controller/BatchController.java`  
**Path:** `/batches/**`  
**Access:** Access varies by endpoint.

### Purpose
CRUD endpoints for managing course batches. Vendors can create, update, and delete batches. Vendors and Partners can list and retrieve batches.

### Endpoints

| HTTP Method | Path | Purpose | Request Body | Response Body | Access |
|---|---|---|---|---|---|
| `POST` | `/batches` | Create a new batch | `BatchCreateDto` | `ApiResponse<BatchProfileResponseDto>` | `VENDOR` |
| `PUT` | `/batches/{id}` | Update batch | `BatchUpdateDto` | `ApiResponse<Void>` | `VENDOR` |
| `DELETE` | `/batches/{id}` | Delete batch | — | `ApiResponse<Void>` | `VENDOR` |
| `GET` | `/batches` | List batches by course ID | Query Param `courseId` | `ApiResponse<List<BatchDataDto>>` | `VENDOR` or `PARTNER` |
| `GET` | `/batches/{id}` | Get batch by ID | — | `ApiResponse<BatchProfileResponseDto>` | `VENDOR` or `PARTNER` |

---

## 6. SessionController.java

**File:** `controller/SessionController.java`  
**Path:** `/sessions/**`  
**Access:** Access varies by endpoint.

### Purpose
CRUD endpoints for managing class/lecture sessions within batches. Vendors can create, update, and delete sessions. Vendors and Partners can list and view sessions.

### Endpoints

| HTTP Method | Path | Purpose | Request Body | Response Body | Access |
|---|---|---|---|---|---|
| `POST` | `/sessions` | Create a new session | `SessionCreateDto` | `ApiResponse<SessionProfileResponseDto>` | `VENDOR` |
| `PUT` | `/sessions/{id}` | Update session | `SessionUpdateDto` | `ApiResponse<Void>` | `VENDOR` |
| `DELETE` | `/sessions/{id}` | Delete session | — | `ApiResponse<Void>` | `VENDOR` |
| `GET` | `/sessions` | List sessions by batch ID | Query Param `batchId` | `ApiResponse<List<SessionDataDto>>` | `VENDOR` or `PARTNER` |
| `GET` | `/sessions/{id}` | Get session by ID | — | `ApiResponse<SessionProfileResponseDto>` | `VENDOR` or `PARTNER` |

---

## 7. PartnerAccessRequestController.java

**File:** `controller/PartnerAccessRequestController.java`  
**Path:** `/access-requests/**`  
**Access:** Access varies by endpoint.

### Purpose
Allows Partners to request access to courses/batches managed by Vendors, and allows Vendors to approve, reject, or delete these requests.

### Endpoints

| HTTP Method | Path | Purpose | Request Body | Response Body | Access |
|---|---|---|---|---|---|
| `POST` | `/access-requests` | Create access request directly (APPROVED status) | `VendorAccessRequestCreateDto` | `ApiResponse<Void>` | `VENDOR` |
| `POST` | `/access-requests/request` | Submit course/batch access request (PENDING status) | `PartnerAccessRequestCreateDto` | `ApiResponse<Void>` | `PARTNER` |
| `PUT` | `/access-requests/{id}/status` | Update request status (approve/reject) | `AccessStatusUpdateDto` | `ApiResponse<Void>` | `VENDOR` |
| `DELETE` | `/access-requests/{id}` | Delete access record | — | `ApiResponse<Void>` | `VENDOR` |
| `GET` | `/access-requests/vendor/{vendorId}` | List requests received by a vendor | — | `ApiResponse<List<AccessRequestResponseDto>>` | `VENDOR` or `SUPER_ADMIN` |
| `GET` | `/access-requests/partner/{partnerId}` | List requests submitted by a partner | — | `ApiResponse<List<AccessRequestResponseDto>>` | `VENDOR` or `SUPER_ADMIN` |

---

## 8. StudentComplaintController.java

**File:** `controller/StudentComplaintController.java`  
**Path:** `/complaints/**`  
**Access:** Access varies by endpoint.

### Purpose
Allows students to submit complaints publicly, and allows Vendors and Partners to view, update status, and leave remarks.

### Endpoints

| HTTP Method | Path | Purpose | Request Body | Response Body | Access |
|---|---|---|---|---|---|
| `POST` | `/complaints` | Submit a student complaint | `ComplaintCreateDto` | `ApiResponse<Void>` | **Public** |
| `PUT` | `/complaints/{id}` | Update status and remarks | `ComplaintUpdateDto` | `ApiResponse<Void>` | `VENDOR` or `PARTNER` |
| `DELETE` | `/complaints/{id}` | Delete a complaint | — | `ApiResponse<Void>` | `VENDOR` |
| `GET` | `/complaints` | List complaints by vendor or partner | Query Param `vendorId` or `partnerId` | `ApiResponse<List<ComplaintResponseDto>>` | `VENDOR` or `PARTNER` |
| `GET` | `/complaints/{id}` | Get complaint details | — | `ApiResponse<ComplaintResponseDto>` | `VENDOR` or `PARTNER` |

---

## 8b. PartnerPortalController.java

**File:** `controller/PartnerPortalController.java`  
**Path:** `/partner-portal/**`  
**Access:** `@PreAuthorize("hasAuthority('VENDOR')")` — **Vendor only**

### Purpose
Allows Vendors to audit, view, and analyze details of specific partners (courses they access, batches under those courses, session engagement with student access counts, and student list with enrollment details).

### Endpoints

| HTTP Method | Path | Purpose | Request Body | Response Body |
|---|---|---|---|---|
| `GET` | `/partner-portal/partner/{partnerId}/courses` | Get courses the partner has access to | — | `ApiResponse<List<CourseDataDto>>` |
| `GET` | `/partner-portal/partner/{partnerId}/courses/{courseId}/batches` | Get batches under course with access data | — | `ApiResponse<List<BatchDataDto>>` |
| `GET` | `/partner-portal/partner/{partnerId}/batches/{batchId}/sessions` | Get sessions with student counts | — | `ApiResponse<List<PartnerSessionResponseDto>>` |
| `GET` | `/partner-portal/partner/{partnerId}/students` | Get students with enrollment data | — | `ApiResponse<List<PartnerStudentResponseDto>>` |

---

# DTOs — Root (`dto/`)

---

## 9. OtpSessionData.java

**File:** `dto/OtpSessionData.java`  
**Type:** Data class (Lombok `@Data` + `@Builder`)

### Purpose
Stores OTP session data in Redis during the password setup/reset flow.

### Fields

| Field | Type | Description |
|---|---|---|
| `username` | String | The username of the user who requested the OTP. |
| `email` | String | The email the OTP was sent to. |
| `otp` | String | The 6-digit OTP code. |

---

# DTOs — Request (`dto/request/`)

Request DTOs are what the client sends in the HTTP request body. They use Jakarta Validation annotations (`@NotBlank`, `@Email`, `@Size`, `@NotNull`) for input validation.

---

## 10. LoginRequestDto.java

**Used by:** `POST /auth/login`

| Field | Validation | Description |
|---|---|---|
| `username` | `@NotBlank` | The user's login username. |
| `password` | `@NotBlank` | The user's password. |

---

## 11. SendOtpRequestDto.java

**Used by:** `POST /auth/send-otp`

| Field | Validation | Description |
|---|---|---|
| `usernameOrEmail` | `@NotBlank` | Either the username or email address. The server resolves the user from both. |

---

## 12. SetPasswordRequestDto.java

**Used by:** `POST /auth/set-password`

| Field | Validation | Description |
|---|---|---|
| `otp` | `@NotBlank` | The 6-digit OTP received via email. |
| `newPassword` | `@NotBlank`, `@Size(min=8)` | The new password (minimum 8 characters). |

---

## 13. SetPasswordDto.java (LEGACY)

**Used by:** Not currently used by any controller.

| Field | Validation | Description |
|---|---|---|
| `username` | `@NotBlank` | Username. |
| `otp` | `@NotBlank` | OTP. |
| `newPassword` | `@NotBlank`, `@Size(min=8)` | New password. |

---

## 14. ForgotUsernameRequestDto.java

**Used by:** `POST /auth/forgot-username`

| Field | Validation | Description |
|---|---|---|
| `email` | `@NotBlank`, `@Email` | The email address to look up. |

---

## 15. VendorCreateDto.java

**Used by:** `POST /vendors`

| Field | Validation | Description |
|---|---|---|
| `username` | `@NotBlank` | Login username for the new vendor. |
| `email` | `@NotBlank`, `@Email` | Login email for the new vendor. |
| `companyName` | `@NotBlank` | The vendor's company/organization name. |
| `phone` | Optional | Contact phone number. |
| `description` | Optional | Company description / bio. |

---

## 16. VendorUpdateDto.java

**Used by:** `PUT /vendors/{id}`

| Field | Validation | Description |
|---|---|---|
| `companyName` | `@NotBlank` | Updated company name. |
| `phone` | Optional | Updated phone number. |
| `description` | Optional | Updated description. |
| `isActive` | Optional (`Boolean`) | Set to `false` to block the vendor, `true` to reactivate. |

---

## 17. PartnerCreateDto.java

**Used by:** `POST /partners`

| Field | Validation | Description |
|---|---|---|
| `username` | `@NotBlank` | Login username for the new partner. |
| `email` | `@NotBlank`, `@Email` | Login email for the new partner. |
| `companyName` | `@NotBlank` | The partner's college/institution name. |
| `phone` | Optional | Contact phone number. |
| `description` | Optional | Organization description. |
| `vendorId` | Optional | Required only when Super Admin creates a partner. |

---

## 18. PartnerUpdateDto.java

**Used by:** `PUT /partners/{id}`

| Field | Validation | Description |
|---|---|---|
| `companyName` | `@NotBlank` | Updated company name. |
| `phone` | Optional | Updated phone number. |
| `description` | Optional | Updated description. |
| `isActive` | Optional (`Boolean`) | Set to `false` to block the partner, `true` to reactivate. |

---

## 19. CourseCreateDto.java

**Used by:** `POST /courses`

| Field | Validation | Description |
|---|---|---|
| `title` | `@NotBlank` | Course title. |
| `subtitle` | Optional | Course subtitle. |
| `description` | `@NotBlank` | Detailed course description. |
| `category` | `@NotBlank` | Course category/genre. |

---

## 20. CourseUpdateDto.java

**Used by:** `PUT /courses/{id}`

| Field | Validation | Description |
|---|---|---|
| `title` | `@NotBlank` | Updated course title. |
| `subtitle` | Optional | Updated course subtitle. |
| `description` | `@NotBlank` | Updated course description. |
| `category` | `@NotBlank` | Updated course category. |

---

## 21. BatchCreateDto.java

**Used by:** `POST /batches`

| Field | Validation | Description |
|---|---|---|
| `courseId` | `@NotNull` | ID of the course this batch belongs to. |
| `title` | `@NotBlank` | Batch title. |
| `subtitle` | Optional | Batch subtitle. |
| `description` | `@NotBlank` | Batch description. |
| `scheduledDate` | `@NotNull` | Date the batch starts/runs. |
| `scheduledTime` | `@NotNull` | Time the batch starts/runs. |

---

## 22. BatchUpdateDto.java

**Used by:** `PUT /batches/{id}`

| Field | Validation | Description |
|---|---|---|
| `title` | `@NotBlank` | Updated batch title. |
| `subtitle` | Optional | Updated batch subtitle. |
| `description` | `@NotBlank` | Updated batch description. |
| `scheduledDate` | `@NotNull` | Updated scheduled date. |
| `scheduledTime` | `@NotNull` | Updated scheduled time. |

---

## 23. SessionCreateDto.java

**Used by:** `POST /sessions`

| Field | Validation | Description |
|---|---|---|
| `courseId` | `@NotNull` | Course ID for the session. |
| `batchId` | `@NotNull` | Batch ID for the session. |
| `sessionType` | `@NotNull` | Type of session (`SessionType` enum: `LIVE`, `RECORDED`, `DOCUMENT`, etc.). |
| `title` | `@NotBlank` | Session title. |
| `subtitle` | Optional | Session subtitle. |
| `description` | `@NotBlank` | Session description. |
| `liveLink` | Optional | URL for live stream/class. |
| `recordedLink` | Optional | URL for recorded session video. |
| `resourceLink` | Optional | URL for materials/documents. |
| `sequenceOrder` | `@NotNull` | Ordering sequence in the batch. |
| `scheduledDate` | Optional | Date of session. |
| `scheduledTime` | Optional | Time of session. |

---

## 24. SessionUpdateDto.java

**Used by:** `PUT /sessions/{id}`

| Field | Validation | Description |
|---|---|---|
| `sessionType` | `@NotNull` | Updated session type. |
| `title` | `@NotBlank` | Updated title. |
| `subtitle` | Optional | Updated subtitle. |
| `description` | `@NotBlank` | Updated description. |
| `liveLink` | Optional | Updated live link. |
| `recordedLink` | Optional | Updated recorded link. |
| `resourceLink` | Optional | Updated resource link. |
| `sequenceOrder` | `@NotNull` | Updated sequence order. |
| `scheduledDate` | Optional | Updated date. |
| `scheduledTime` | Optional | Updated time. |

## 25. VendorAccessRequestCreateDto.java

**Used by:** `POST /access-requests` (by Vendor)

| Field | Validation | Description |
|---|---|---|
| `courseId` | `@NotNull` | Target course ID. |
| `batchId` | Optional | Target batch ID. |
| `courseName` | `@NotBlank` | Name of the course. |
| `batchName` | Optional | Name of the batch. |
| `partnerId` | `@NotNull` | Target partner ID. |
| `partnerName` | `@NotBlank` | Target partner's name. |
| `maxStudents` | `@NotNull` | Maximum students allowed. |
| `note` | Optional | Response/resolution note. |

---

## 25b. PartnerAccessRequestCreateDto.java

**Used by:** `POST /access-requests/request` (by Partner)

| Field | Validation | Description |
|---|---|---|
| `courseId` | `@NotNull` | Target course ID. |
| `courseName` | `@NotBlank` | Name of the course. |
| `batchId` | Optional | Target batch ID. |
| `batchName` | Optional | Name of the batch. |
| `partnerId` | `@NotNull` | Partner ID. |
| `partnerName` | `@NotBlank` | Partner name. |
| `maxStudents` | `@NotNull` | Maximum students requested. |
| `note` | Optional | Request note. |

---

## 26. AccessStatusUpdateDto.java

**Used by:** `PUT /access-requests/{id}/status`

| Field | Validation | Description |
|---|---|---|
| `status` | `@NotNull` | Updated status (`AccessRequestStatus` enum). |
| `responseNote` | Optional | Note added by the vendor on resolution. |
| `maxStudents` | Optional | Update maximum students permitted. |
| `courseId` | Optional | Update course ID. |
| `courseName` | Optional | Update course name. |
| `batchId` | Optional | Update batch ID. |
| `batchName` | Optional | Update batch name. |

---

## 27. ComplaintCreateDto.java

**Used by:** `POST /complaints`

| Field | Validation | Description |
|---|---|---|
| `studentId` | `@NotNull` | Student ID. |
| `partnerId` | `@NotNull` | Partner ID. |
| `vendorId` | `@NotNull` | Vendor ID. |
| `courseId` | `@NotNull` | Course ID. |
| `batchId` | `@NotNull` | Batch ID. |
| `studentName` | `@NotBlank` | Student's name. |
| `email` | `@NotBlank`, `@Email` | Student's contact email. |
| `phoneNumber` | Optional | Student's phone number. |
| `college` | Optional | College/institution name. |
| `branch` | Optional | Academic branch/course stream. |
| `academicYear` | Optional | Current academic year. |
| `courseName` | `@NotBlank` | Name of the course. |
| `batchName` | `@NotBlank` | Name of the batch. |
| `complaintTitle` | `@NotBlank` | Short summary/title of the issue. |
| `complaintText` | `@NotBlank` | Detailed explanation of the complaint. |

---

## 28. ComplaintUpdateDto.java

**Used by:** `PUT /complaints/{id}`

| Field | Validation | Description |
|---|---|---|
| `status` | `@NotNull` | Status of the complaint (`ComplaintStatus` enum). |
| `vendorRemark` | Optional | Remark left by the Vendor. |
| `partnerRemark` | Optional | Remark left by the Partner. |

---

# DTOs — Response (`dto/response/`)

---

## 29. ApiResponse.java

**Type:** Generic wrapper `ApiResponse<T>`

### Purpose
The **universal response envelope** for every API endpoint.

---

## 30. LoginResponseDto.java

**Used by:** `POST /auth/login`, `GET /auth/session`

### Fields

| Field | Type | Description |
|---|---|---|
| `user` | `UserDataDto` | Always present. |
| `vendor` | `VendorDataDto` | Present for VENDOR and PARTNER roles. |
| `partner` | `PartnerDataDto` | Present only for PARTNER role. |

---

## 31. UserDataDto.java

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | String | User UUID. |
| `username` | String | Login username. |
| `email` | String | Login email. |
| `role` | String | `SUPER_ADMIN`, `VENDOR`, or `PARTNER`. |
| `status` | String | Account status. |
| `emailVerified` | boolean | Email status. |
| `lastLoginAt` | String | Timestamp. |
| `createdAt` | String | Timestamp. |
| `updatedAt` | String | Timestamp. |
| `deviceDetails` | `DeviceDetailsDto` | Device metadata. |

---

## 32. VendorDataDto.java

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | String | Vendor UUID. |
| `companyName` | String | Vendor's company name. |
| `phone` | String | Contact phone. |
| `description` | String | Company bio. |
| `active` | boolean | Active status. |
| `createdAt` | String | Creation timestamp. |
| `deviceDetails` | `DeviceDetailsDto` | Device details. |

---

## 33. PartnerDataDto.java

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | String | Partner UUID. |
| `companyName` | String | Partner's institution name. |
| `phone` | String | Contact phone. |
| `description` | String | Organization bio. |
| `active` | boolean | Active status. |
| `vendorId` | String | UUID of parent vendor. |
| `createdAt` | String | Creation timestamp. |
| `deviceDetails` | `DeviceDetailsDto` | Device details. |

---

## 34. DeviceDetailsDto.java

**Purpose:** Captures client device metadata sent via custom HTTP headers during login.

---

## 35. AuthResponseDto.java (LEGACY)

---

## 36. VendorProfileResponseDto.java

---

## 37. PartnerProfileResponseDto.java

---

## 38. CourseDataDto.java

**Purpose:** Flat representation of a `Course` entity for list responses.

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Course UUID. |
| `title` | String | Course title. |
| `subtitle` | String | Course subtitle. |
| `description` | String | Course description. |
| `category` | String | Course category. |
| `createdAt` | String | Creation timestamp. |
| `updatedAt` | String | Last update timestamp. |

---

## 39. CourseProfileResponseDto.java

**Purpose:** Detailed representation of a `Course` entity.

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Course UUID. |
| `title` | String | Course title. |
| `subtitle` | String | Course subtitle. |
| `description` | String | Course description. |
| `category` | String | Course category. |
| `createdAt` | String | Creation timestamp. |
| `updatedAt` | String | Last update timestamp. |

---

## 40. BatchDataDto.java

**Purpose:** Flat representation of a `Batch` entity.

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Batch UUID. |
| `courseId` | UUID | Parent course UUID. |
| `title` | String | Batch title. |
| `subtitle` | String | Batch subtitle. |
| `description` | String | Batch description. |
| `scheduledDate` | String | Scheduled start/run date. |
| `scheduledTime` | String | Scheduled start/run time. |
| `createdAt` | String | Creation timestamp. |
| `updatedAt` | String | Last update timestamp. |

---

## 41. BatchProfileResponseDto.java

**Purpose:** Detailed representation of a `Batch` entity.

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Batch UUID. |
| `courseId` | UUID | Parent course UUID. |
| `title` | String | Batch title. |
| `subtitle` | String | Batch subtitle. |
| `description` | String | Batch description. |
| `scheduledDate` | String | Scheduled start/run date. |
| `scheduledTime` | String | Scheduled start/run time. |
| `createdAt` | String | Creation timestamp. |
| `updatedAt` | String | Last update timestamp. |

---

## 42. SessionDataDto.java

**Purpose:** Flat representation of a `Session` entity.

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Session UUID. |
| `courseId` | UUID | Course UUID. |
| `batchId` | UUID | Batch UUID. |
| `sessionType` | String | Type of session (`LIVE`, `RECORDED`, etc.). |
| `title` | String | Session title. |
| `subtitle` | String | Session subtitle. |
| `description` | String | Session description. |
| `liveLink` | String | Live URL link. |
| `recordedLink` | String | Recorded video URL link. |
| `resourceLink` | String | Resource/materials URL link. |
| `sequenceOrder` | Integer | Sequence order in batch. |
| `scheduledDate` | String | Date. |
| `scheduledTime` | String | Time. |
| `createdAt` | String | Creation timestamp. |
| `updatedAt` | String | Last update timestamp. |

---

## 43. SessionProfileResponseDto.java

**Purpose:** Detailed representation of a `Session` entity.

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Session UUID. |
| `courseId` | UUID | Course UUID. |
| `batchId` | UUID | Batch UUID. |
| `sessionType` | String | Type of session. |
| `title` | String | Session title. |
| `subtitle` | String | Session subtitle. |
| `description` | String | Session description. |
| `liveLink` | String | Live link. |
| `recordedLink` | String | Recorded link. |
| `resourceLink` | String | Resource link. |
| `sequenceOrder` | Integer | Order index. |
| `scheduledDate` | String | Date. |
| `scheduledTime` | String | Time. |
| `createdAt` | String | Creation timestamp. |
| `updatedAt` | String | Last update timestamp. |

---

## 44. AccessRequestResponseDto.java

**Purpose:** DTO representing a partner's course/batch access request status and remarks.

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Request ID. |
| `partnerId` | UUID | Partner UUID. |
| `vendorId` | UUID | Vendor UUID. |
| `courseId` | UUID | Course UUID. |
| `batchId` | UUID | Batch UUID. |
| `partnerName` | String | Partner organization name. |
| `courseName` | String | Course name. |
| `batchName` | String | Batch name. |
| `status` | String | Request status (`PENDING`, `APPROVED`, `REJECTED`). |
| `requestNote` | String | Requester's explanation. |
| `responseNote` | String | Approver's resolution note. |
| `requestedAt` | String | Request timestamp. |
| `resolvedAt` | String | Resolution timestamp. |

---

## 45. ComplaintResponseDto.java

**Purpose:** DTO representing a student complaint.

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Complaint ID. |
| `studentId` | UUID | Student ID. |
| `partnerId` | UUID | Partner ID. |
| `vendorId` | UUID | Vendor ID. |
| `courseId` | UUID | Course ID. |
| `batchId` | UUID | Batch ID. |
| `studentName` | String | Student's name. |
| `email` | String | Student's email. |
| `phoneNumber` | String | Student's phone. |
| `college` | String | College name. |
| `branch` | String | Branch stream. |
| `academicYear` | String | Academic year. |
| `courseName` | String | Course name. |
| `batchName` | String | Batch name. |
| `complaintTitle` | String | Title of complaint. |
| `complaintText` | String | Content/details of complaint. |
| `status` | String | Status (`PENDING`, `RESOLVED`, etc.). |
| `vendorRemark` | String | Remark from Vendor. |
| `partnerRemark` | String | Remark from Partner. |
| `createdAt` | String | Creation timestamp. |
| `updatedAt` | String | Last update timestamp. |
