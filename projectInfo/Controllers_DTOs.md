# Bleep LearnHub — Controllers & DTOs Documentation

This document provides a detailed explanation of every file in the `controller/` and `dto/` packages — what each file does, why it exists, and what it contains.

---

## Table of Contents

**Controllers (`controller/`)**
1. [AuthController.java](#1-authcontrollerjava)
2. [VendorController.java](#2-vendorcontrollerjava)
3. [PartnerController.java](#3-partnercontrollerjava)

**DTOs — Root (`dto/`)**
4. [OtpSessionData.java](#4-otpsessiondatajava)

**DTOs — Request (`dto/request/`)**
5. [LoginRequestDto.java](#5-loginrequestdtojava)
6. [SendOtpRequestDto.java](#6-sendotprequestdtojava)
7. [SetPasswordRequestDto.java](#7-setpasswordrequestdtojava)
8. [SetPasswordDto.java](#8-setpassworddtojava-legacy)
9. [ForgotUsernameRequestDto.java](#9-forgotusernamerequestdtojava)
10. [VendorCreateDto.java](#10-vendorcreatedtojava)
11. [VendorUpdateDto.java](#11-vendorupdatedtojava)
12. [PartnerCreateDto.java](#12-partnercreatedtojava)
13. [PartnerUpdateDto.java](#13-partnerupdatedtojava)

**DTOs — Response (`dto/response/`)**
14. [ApiResponse.java](#14-apiresponsejava)
15. [LoginResponseDto.java](#15-loginresponsedtojava)
16. [UserDataDto.java](#16-userdatadtojava)
17. [VendorDataDto.java](#17-vendordatadtojava)
18. [PartnerDataDto.java](#18-partnerdatadtojava)
19. [DeviceDetailsDto.java](#19-devicedetailsdtojava)
20. [AuthResponseDto.java](#20-authresponsedtojava-legacy)
21. [VendorProfileResponseDto.java](#21-vendorprofileresponsedtojava)
22. [PartnerProfileResponseDto.java](#22-partnerprofileresponsedtojava)

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

### How Login Works
1. Reads device details from custom headers (`Device-Ip`, `Device-Type`, `Device`, `Device-Model`, `OS-Name`, `OS-Version`, `Client-Name`, `Client-Version`).
2. Calls `AuthService.login()` which returns a `LoginResult(sessionId, loginResponseDto)`.
3. Uses `CookieService` to build an HttpOnly `session_id` cookie and adds it to the response.
4. The response body contains the `LoginResponseDto` — user profile + vendor/partner data based on role.

### How Session Works
1. Extracts `session_id` cookie from the request.
2. Calls `AuthService.getSession()` which reads from Redis (no DB hit).
3. Returns 401 if no cookie or if Redis session has expired.

### How OTP Flow Works
1. **Send OTP:** Client POSTs username/email → server generates OTP, stores in Redis, sends email, and returns `otp_session` cookie.
2. **Set Password:** Client POSTs OTP + new password → server reads `otp_session` cookie → validates OTP from Redis → updates password → clears the cookie.

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

### Notes
- Creating a vendor also creates a `User` with `VENDOR` role in `PENDING_SETUP` status and sends a welcome email.
- Deleting a vendor **cascades** — all partners under the vendor and their users are deleted.
- Updating `isActive` to `false` blocks the vendor user and invalidates all their active sessions.

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

### Authorization Logic
Every method extracts the caller's `username` and checks if they have `SUPER_ADMIN` authority:
- **Super Admin:** Can access all partners across all vendors.
- **Vendor:** Can only access partners that belong to their vendor account. Ownership is validated in the service layer.

---

# DTOs — Root (`dto/`)

---

## 4. OtpSessionData.java

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

### Lifecycle
1. Created when `AuthService.sendOtp()` generates an OTP.
2. Stored in Redis at key `otp_session:{token}` with a 30-minute TTL.
3. Read when `AuthService.setPassword()` validates the OTP.
4. Deleted after successful password set.

---

# DTOs — Request (`dto/request/`)

Request DTOs are what the client sends in the HTTP request body. They use Jakarta Validation annotations (`@NotBlank`, `@Email`, `@Size`) for input validation.

---

## 5. LoginRequestDto.java

**Used by:** `POST /auth/login`

| Field | Validation | Description |
|---|---|---|
| `username` | `@NotBlank` | The user's login username. |
| `password` | `@NotBlank` | The user's password. |

---

## 6. SendOtpRequestDto.java

**Used by:** `POST /auth/send-otp`

| Field | Validation | Description |
|---|---|---|
| `usernameOrEmail` | `@NotBlank` | Either the username or email address. The server resolves the user from both. |

---

## 7. SetPasswordRequestDto.java

**Used by:** `POST /auth/set-password`

| Field | Validation | Description |
|---|---|---|
| `otp` | `@NotBlank` | The 6-digit OTP received via email. |
| `newPassword` | `@NotBlank`, `@Size(min=8)` | The new password (minimum 8 characters). |

**Note:** The user identity is resolved from the `otp_session` cookie (which maps to the OTP data in Redis), not from this payload. This prevents users from changing someone else's password.

---

## 8. SetPasswordDto.java (LEGACY)

**Used by:** Not currently used by any controller.

| Field | Validation | Description |
|---|---|---|
| `username` | `@NotBlank` | Username. |
| `otp` | `@NotBlank` | OTP. |
| `newPassword` | `@NotBlank`, `@Size(min=8)` | New password. |

**Note:** This is the older version that includes a `username` field in the body. It has been superseded by `SetPasswordRequestDto` which resolves the user from the `otp_session` cookie instead. Kept for backward compatibility but should be removed in a future cleanup.

---

## 9. ForgotUsernameRequestDto.java

**Used by:** `POST /auth/forgot-username`

| Field | Validation | Description |
|---|---|---|
| `email` | `@NotBlank`, `@Email` | The email address to look up. Must be a valid email format. |

---

## 10. VendorCreateDto.java

**Used by:** `POST /vendors`

| Field | Validation | Description |
|---|---|---|
| `username` | `@NotBlank` | Login username for the new vendor. |
| `email` | `@NotBlank`, `@Email` | Login email for the new vendor. |
| `companyName` | `@NotBlank` | The vendor's company/organization name. |
| `phone` | Optional | Contact phone number. |
| `description` | Optional | Company description / bio. |

---

## 11. VendorUpdateDto.java

**Used by:** `PUT /vendors/{id}`

| Field | Validation | Description |
|---|---|---|
| `companyName` | `@NotBlank` | Updated company name. |
| `phone` | Optional | Updated phone number. |
| `description` | Optional | Updated description. |
| `isActive` | Optional (`Boolean`) | Set to `false` to block the vendor, `true` to reactivate. |

---

## 12. PartnerCreateDto.java

**Used by:** `POST /partners`

| Field | Validation | Description |
|---|---|---|
| `username` | `@NotBlank` | Login username for the new partner. |
| `email` | `@NotBlank`, `@Email` | Login email for the new partner. |
| `companyName` | `@NotBlank` | The partner's college/institution name. |
| `phone` | Optional | Contact phone number. |
| `description` | Optional | Organization description. |
| `vendorId` | Optional | **Required only when Super Admin creates a partner** (to specify which vendor the partner belongs to). Vendors don't need this — they are auto-resolved. |

---

## 13. PartnerUpdateDto.java

**Used by:** `PUT /partners/{id}`

| Field | Validation | Description |
|---|---|---|
| `companyName` | `@NotBlank` | Updated company name. |
| `phone` | Optional | Updated phone number. |
| `description` | Optional | Updated description. |
| `isActive` | Optional (`Boolean`) | Set to `false` to block the partner, `true` to reactivate. |

---

# DTOs — Response (`dto/response/`)

Response DTOs are what the server returns in the HTTP response body. They are designed to expose only the data the client needs, hiding internal details like password hashes.

---

## 14. ApiResponse.java

**Type:** Generic wrapper `ApiResponse<T>`

### Purpose
The **universal response envelope** for every API endpoint. Ensures all responses have a consistent JSON shape.

### Structure

```json
{
  "data": { ... },        // The actual payload (null on failure)
  "message": "Success",   // Human-readable message (null on failure)
  "error": null,          // Error description (null on success)
  "errorCode": null       // Machine-readable error code (null on success)
}
```

### Factory Methods

| Method | When Used |
|---|---|
| `ApiResponse.success(data, message)` | Successful response with data. |
| `ApiResponse.success(message)` | Successful response without data (e.g. "Deleted successfully"). |
| `ApiResponse.failure(error, errorCode)` | Error response (e.g. "Session expired", "SESSION_EXPIRED"). |

---

## 15. LoginResponseDto.java

**Type:** Composite response DTO  
**Used by:** `POST /auth/login`, `GET /auth/session`  
**Also stored in:** Redis as the session payload

### Purpose
The **primary login/session payload**. Contains everything the frontend needs to render the dashboard after login.

### Structure by Role

| Role | Fields Present |
|---|---|
| `SUPER_ADMIN` | `user` only |
| `VENDOR` | `user` + `vendor` |
| `PARTNER` | `user` + `vendor` (parent) + `partner` |

### Fields

| Field | Type | Description |
|---|---|---|
| `user` | `UserDataDto` | Always present — user profile data. |
| `vendor` | `VendorDataDto` | Present for VENDOR and PARTNER roles. For PARTNER, this is the parent vendor. |
| `partner` | `PartnerDataDto` | Present only for PARTNER role. |

Uses `@JsonInclude(NON_NULL)` so null fields are omitted from the JSON output.

---

## 16. UserDataDto.java

**Purpose:** Flat representation of a `User` entity, excluding sensitive fields like `passwordHash`.

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | String | User UUID. |
| `username` | String | Login username. |
| `email` | String | Login email. |
| `role` | String | `SUPER_ADMIN`, `VENDOR`, or `PARTNER`. |
| `status` | String | `PENDING_SETUP`, `ACTIVE`, or `BLOCKED`. |
| `emailVerified` | boolean | Whether email has been verified. |
| `lastLoginAt` | String | ISO timestamp of last login. |
| `createdAt` | String | ISO timestamp of account creation. |
| `updatedAt` | String | ISO timestamp of last update. |
| `deviceDetails` | `DeviceDetailsDto` | Device info captured at login time. |

---

## 17. VendorDataDto.java

**Purpose:** Flat representation of a `Vendor` entity for login/session responses.

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | String | Vendor UUID. |
| `companyName` | String | Vendor's company name. |
| `phone` | String | Contact phone. |
| `description` | String | Company bio. |
| `active` | boolean | Whether the vendor is active. |
| `createdAt` | String | ISO timestamp. |
| `deviceDetails` | `DeviceDetailsDto` | Device info captured at login. |

---

## 18. PartnerDataDto.java

**Purpose:** Flat representation of a `Partner` entity for login/session responses.

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | String | Partner UUID. |
| `companyName` | String | Partner's institution name. |
| `phone` | String | Contact phone. |
| `description` | String | Organization bio. |
| `active` | boolean | Whether the partner is active. |
| `vendorId` | String | UUID of the parent vendor. |
| `createdAt` | String | ISO timestamp. |
| `deviceDetails` | `DeviceDetailsDto` | Device info captured at login. |

---

## 19. DeviceDetailsDto.java

**Purpose:** Captures client device metadata sent via custom HTTP headers during login.

### Fields

| Field | HTTP Header | Description |
|---|---|---|
| `deviceIp` | `Device-Ip` (fallback: `RemoteAddr`) | Client IP address. |
| `deviceType` | `Device-Type` | e.g. "MOBILE", "WEB", "DESKTOP". |
| `device` | `Device` | Device brand/name. |
| `deviceModel` | `Device-Model` | Specific model (e.g. "iPhone 15 Pro"). |
| `osName` | `OS-Name` | Operating system (e.g. "iOS", "Windows"). |
| `osVersion` | `OS-Version` | OS version string. |
| `clientName` | `Client-Name` | Browser or app name. |
| `clientVersion` | `Client-Version` | Browser or app version. |

### Where It's Stored
- Inside `UserDataDto`, `VendorDataDto`, and `PartnerDataDto` in the Redis session.
- In the `UserSession` database table for audit history.

---

## 20. AuthResponseDto.java (LEGACY)

**Purpose:** An older response DTO from when the app used JWT tokens in response bodies.

### Fields

| Field | Type | Description |
|---|---|---|
| `accessToken` | String | The JWT access token. |
| `role` | String | User role. |
| `username` | String | Username. |

**Note:** This DTO is **no longer used** by any controller. The app now uses `LoginResponseDto` with cookie-based sessions. Kept for potential future use or backward compatibility.

---

## 21. VendorProfileResponseDto.java

**Purpose:** Detailed vendor profile returned by the `VendorController` CRUD endpoints (different from `VendorDataDto` used in login payloads).

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | String | Vendor UUID. |
| `username` | String | Login username. |
| `email` | String | Contact email. |
| `companyName` | String | Company name. |
| `phone` | String | Phone number. |
| `description` | String | Company bio. |
| `status` | String | Account status (`ACTIVE`, `PENDING_SETUP`, `BLOCKED`). |
| `isActive` | boolean | Active flag. |
| `joinedAt` | `LocalDateTime` | When the vendor was created. |

### Difference from VendorDataDto
`VendorProfileResponseDto` includes `username`, `email`, `status`, and `joinedAt` — fields needed for the admin management UI. `VendorDataDto` is a lighter payload included in login/session responses.

---

## 22. PartnerProfileResponseDto.java

**Purpose:** Detailed partner profile returned by the `PartnerController` CRUD endpoints.

### Fields

| Field | Type | Description |
|---|---|---|
| `id` | String | Partner UUID. |
| `username` | String | Login username. |
| `email` | String | Contact email. |
| `companyName` | String | Institution name. |
| `phone` | String | Phone number. |
| `description` | String | Organization bio. |
| `parentVendorId` | String | UUID of the parent vendor. |
| `parentVendorCompanyName` | String | Name of the parent vendor. |
| `isActive` | boolean | Active flag. |

### Difference from PartnerDataDto
`PartnerProfileResponseDto` includes `username`, `email`, and parent vendor details — designed for the admin management UI. `PartnerDataDto` is for login/session payloads.

---

# DTO Naming Convention Summary

| Suffix | Purpose | Example |
|---|---|---|
| `RequestDto` | HTTP request body (client → server) | `LoginRequestDto`, `SendOtpRequestDto` |
| `CreateDto` | Request body for entity creation | `VendorCreateDto`, `PartnerCreateDto` |
| `UpdateDto` | Request body for entity update | `VendorUpdateDto`, `PartnerUpdateDto` |
| `ResponseDto` | HTTP response body (server → client) | `LoginResponseDto`, `ApiResponse` |
| `DataDto` | Entity data included in composite responses | `UserDataDto`, `VendorDataDto` |
| `ProfileResponseDto` | Detailed entity profile for admin CRUD | `VendorProfileResponseDto` |
