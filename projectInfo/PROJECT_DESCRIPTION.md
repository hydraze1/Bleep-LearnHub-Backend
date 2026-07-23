# Bleep LearnHub Backend - Project Description

## Project Overview
This is a Spring Boot backend application for the Bleep LearnHub platform. It manages entities such as Students, Courses, Batches, Vendors, Partners, and handles enrollments, complaints, and user sessions. It provides a secure API for various frontends to interact with the system.

## Project Structure
The project follows a standard multi-layer Spring Boot architecture: Controllers, Services, Repositories, Entities (Models), DTOs, Security, and Configuration.

## Configurations
### `SecurityConfig.java`
Handles configuration for Security.

### `ApiLoggingFilter.java`
Handles configuration for ApiLoggingFilter.

### `MasterLoggingFilter.java`
Handles configuration for MasterLoggingFilter.

### `LayerLoggingAspect.java`
Handles configuration for LayerLoggingAspect.

### `RequestTraceFilter.java`
Handles configuration for RequestTraceFilter.

### `RedisConfig.java`
Handles configuration for Redis.

### `AdminSeeder.java`
Handles configuration for AdminSeeder.

### `OpenApiConfig.java`
Handles configuration for OpenApi.

### `MailConfig.java`
Handles configuration for Mail.

## Enums
### `AccountStatus.java`
Values: PENDING_SETUP, ACTIVE

### `AccessRequestStatus.java`
Values: PENDING, APPROVED

### `Role.java`
Values: SUPER_ADMIN, VENDOR

### `ComplaintStatus.java`
Values: PENDING, IN_PROGRESS

### `EnrollmentStatus.java`
Values: ENROLLED, REMOVED

### `SessionType.java`
Values: CLASS, NOTE, ASSIGNMENT

## Entities (Database Tables)
### `AuditLog` (Table: `audit_logs`)
Fields:
- `id` (UUID)
- `user` (User)
- `action` (String)
- `entityName` (String)
- `entityId` (String)
- `ipAddress` (String)
- `payload` (String)
- `createdAt` (LocalDateTime)

### `PartnerAccessRequest` (Table: `partneraccessrequest`)
Fields:
- `id` (UUID)
- `partnerId` (UUID)
- `vendorId` (UUID)
- `courseId` (UUID)
- `batchId` (UUID)
- `partnerName` (String)
- `courseName` (String)
- `batchName` (String)
- `status` (AccessRequestStatus)
- `requestNote` (String)
- `hasBatchAccess` (Boolean)
- `responseNote` (String)
- `maxStudents` (Integer)
- `requestedAt` (LocalDateTime)
- `resolvedAt` (LocalDateTime)

### `StudentComplaint` (Table: `studentcomplaint`)
Fields:
- `id` (UUID)
- `studentId` (UUID)
- `partnerId` (UUID)
- `vendorId` (UUID)
- `courseId` (UUID)
- `batchId` (UUID)
- `studentName` (String)
- `email` (String)
- `phoneNumber` (String)
- `college` (String)
- `branch` (String)
- `academicYear` (String)
- `courseName` (String)
- `batchName` (String)
- `partnerName` (String)
- `vendorName` (String)
- `complaintTitle` (String)
- `complaintText` (String)
- `status` (ComplaintStatus)
- `vendorRemark` (String)
- `partnerRemark` (String)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

### `Student` (Table: `student`)
Fields:
- `id` (UUID)
- `partnerId` (UUID)
- `firstName` (String)
- `lastName` (String)
- `fullName` (String)
- `email` (String)
- `phoneNumber` (String)
- `college` (String)
- `branch` (String)
- `academicYear` (String)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

### `StudentEnrollment` (Table: `studentenrollment`)
Fields:
- `id` (UUID)
- `studentId` (UUID)
- `courseId` (UUID)
- `batchId` (UUID)
- `courseName` (String)
- `batchName` (String)
- `status` (EnrollmentStatus)
- `enrolledAt` (LocalDateTime)
- `completedAt` (LocalDateTime)

### `Batch` (Table: `batch`)
Fields:
- `id` (UUID)
- `courseId` (UUID)
- `title` (String)
- `subtitle` (String)
- `description` (String)
- `startingDate` (LocalDate)
- `endingDate` (LocalDate)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

### `Partner` (Table: `partners`)
Fields:
- `id` (UUID)
- `user` (User)
- `vendor` (Vendor)
- `email` (String)
- `companyName` (String)
- `phone` (String)
- `description` (String)
- `createdAt` (LocalDateTime)

### `Course` (Table: `courses`)
Fields:
- `id` (UUID)
- `title` (String)
- `subtitle` (String)
- `description` (String)
- `category` (String)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

### `UserSession` (Table: `user_sessions`)
Fields:
- `id` (UUID)
- `user` (User)
- `sessionId` (String)
- `ipAddress` (String)
- `browser` (String)
- `os` (String)
- `deviceType` (String)
- `device` (String)
- `deviceModel` (String)
- `osVersion` (String)
- `clientVersion` (String)
- `loginAt` (LocalDateTime)
- `logoutAt` (LocalDateTime)

### `StudentSessionLog` (Table: `studentsessionlog`)
Fields:
- `id` (UUID)
- `studentId` (UUID)
- `sessionId` (UUID)
- `sessionType` (SessionType)
- `courseId` (UUID)
- `courseName` (String)
- `batchId` (UUID)
- `batchName` (String)
- `entryTime` (LocalDateTime)
- `completionTime` (LocalDateTime)
- `timeSpentSec` (Integer)

### `Session` (Table: `session`)
Fields:
- `id` (UUID)
- `courseId` (UUID)
- `batchId` (UUID)
- `sessionType` (SessionType)
- `title` (String)
- `subtitle` (String)
- `description` (String)
- `liveLink` (String)
- `recordedLink` (String)
- `resourceLink` (String)
- `sequenceOrder` (Integer)
- `scheduledDate` (LocalDate)
- `scheduledTime` (LocalTime)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

### `Vendor` (Table: `vendors`)
Fields:
- `id` (UUID)
- `user` (User)
- `email` (String)
- `companyName` (String)
- `phone` (String)
- `description` (String)
- `createdAt` (LocalDateTime)

### `ApiLog` (Table: `api_logs`)
Fields:
- `id` (UUID)
- `username` (String)
- `role` (String)
- `sessionId` (String)
- `ipAddress` (String)
- `url` (String)
- `method` (String)
- `requestBody` (String)
- `statusCode` (int)
- `isError` (boolean)
- `responseBody` (String)
- `errorMessage` (String)
- `successMessage` (String)
- `executionTimeMs` (long)
- `createdAt` (LocalDateTime)

### `User` (Table: `users`)
Fields:
- `id` (UUID)
- `username` (String)
- `email` (String)
- `passwordHash` (String)
- `role` (Role)
- `status` (AccountStatus)
- `lastLoginAt` (LocalDateTime)
- `createdBy` (User)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

## DTOs (Request/Response)
- `StudentDataSyncRequest.java`
- `OtpSessionData.java`
- `PartnerSessionResponseDto.java`
- `UserDataDto.java`
- `PartnerCalendarDayDto.java`
- `PartnerStudentResponseDto.java`
- `ComplaintResponseDto.java`
- `CourseProfileResponseDto.java`
- `ApiResponse.java`
- `PartnerDataDto.java`
- `StudentDataSyncResponseDto.java`
- `PartnerCalendarSessionDto.java`
- `DeviceDetailsDto.java`
- `AuthResponseDto.java`
- `CourseDataDto.java`
- `SessionDataDto.java`
- `VendorDataDto.java`
- `PartnerAccessSessionDto.java`
- `SessionProfileResponseDto.java`
- `PartnerProfileResponseDto.java`
- `BatchProfileResponseDto.java`
- `LoginResponseDto.java`
- `PartnerDashboardResponseDto.java`
- `AccessRequestResponseDto.java`
- `VendorProfileResponseDto.java`
- `BatchDataDto.java`
- `VendorDashboardResponseDto.java`
- `ComplaintCreateDto.java`
- `SessionReorderRequestDto.java`
- `CourseUpdateDto.java`
- `SessionUpdateDto.java`
- `PartnerUpdateDto.java`
- `CourseCreateDto.java`
- `PartnerCreateDto.java`
- `ForgotUsernameRequestDto.java`
- `PartnerAccessRequestCreateDto.java`
- `VendorCreateDto.java`
- `AccessStatusUpdateDto.java`
- `BatchUpdateDto.java`
- `SessionCreateDto.java`
- `SendOtpRequestDto.java`
- `BatchCreateDto.java`
- `ComplaintUpdateDto.java`
- `VendorAccessRequestCreateDto.java`
- `SetPasswordDto.java`
- `LoginRequestDto.java`
- `SessionSequenceDto.java`
- `SetPasswordRequestDto.java`
- `VendorUpdateDto.java`

## Repositories
- `UserSessionRepository.java`: Interface for database operations related to the respective entity.
- `CourseRepository.java`: Interface for database operations related to the respective entity.
- `UserRepository.java`: Interface for database operations related to the respective entity.
- `BatchRepository.java`: Interface for database operations related to the respective entity.
- `VendorRepository.java`: Interface for database operations related to the respective entity.
- `StudentRepository.java`: Interface for database operations related to the respective entity.
- `StudentSessionLogRepository.java`: Interface for database operations related to the respective entity.
- `PartnerAccessRequestRepository.java`: Interface for database operations related to the respective entity.
- `StudentEnrollmentRepository.java`: Interface for database operations related to the respective entity.
- `ApiLogRepository.java`: Interface for database operations related to the respective entity.
- `PartnerRepository.java`: Interface for database operations related to the respective entity.
- `StudentComplaintRepository.java`: Interface for database operations related to the respective entity.
- `SessionRepository.java`: Interface for database operations related to the respective entity.
- `AuditLogRepository.java`: Interface for database operations related to the respective entity.

## Services
- `AdminService.java`: Contains business logic for the respective domain.
- `StudentDataSyncService.java`: Contains business logic for the respective domain.
- `EmailService.java`: Contains business logic for the respective domain.
- `PartnerService.java`: Contains business logic for the respective domain.
- `VendorService.java`: Contains business logic for the respective domain.
- `SessionService.java`: Contains business logic for the respective domain.
- `PartnerPortalService.java`: Contains business logic for the respective domain.
- `AuditLogService.java`: Contains business logic for the respective domain.
- `ApiLogService.java`: Contains business logic for the respective domain.
- `PartnerAccessRequestService.java`: Contains business logic for the respective domain.
- `DashboardService.java`: Contains business logic for the respective domain.
- `StudentComplaintService.java`: Contains business logic for the respective domain.
- `RedisService.java`: Contains business logic for the respective domain.
- `BatchService.java`: Contains business logic for the respective domain.
- `CourseService.java`: Contains business logic for the respective domain.
- `PartnerAccessService.java`: Contains business logic for the respective domain.
- `MasterDataService.java`: Contains business logic for the respective domain.
- `AuthService.java`: Contains business logic for the respective domain.
- `DashboardServiceImpl.java`: Contains business logic for the respective domain.

## Controllers & APIs
### `DashboardController.java`
Base Route: `/dashboard`
- Endpoint: `/vendor/{vendorId}`
- Endpoint: `/partner/{partnerId}`
- Endpoint: `/vendor/{vendorId}/profile`
- Endpoint: `/vendor/{vendorId}/profile`
- Endpoint: `/partner/{partnerId}/profile`
- Endpoint: `/partner/{partnerId}/profile`

### `AuditLogController.java`
Base Route: `/admin/audit-logs`
- Endpoint: `/{id}`

### `PartnerController.java`
Base Route: `/partners`
- Endpoint: `/vendor/{vendorId}`
- Endpoint: `/{id}`
- Endpoint: `/{id}`
- Endpoint: `/{id}`

### `PartnerAccessController.java`
Base Route: `/partner-access`
- Endpoint: `/courses`
- Endpoint: `/batches`
- Endpoint: `/sessions`
- Endpoint: `/schedule`

### `VendorController.java`
Base Route: `/vendors`
- Endpoint: `/{id}`
- Endpoint: `/{id}`
- Endpoint: `/{id}`

### `StudentComplaintController.java`
Base Route: `/complaints`
- Endpoint: `/{id}`
- Endpoint: `/{id}`
- Endpoint: `/{id}`

### `PartnerPortalController.java`
Base Route: `/partner-portal`
- Endpoint: `/partner/{partnerId}/courses`
- Endpoint: `/partner/{partnerId}/courses/{courseId}/batches`
- Endpoint: `/partner/{partnerId}/batches/{batchId}/sessions`
- Endpoint: `/partner/{partnerId}/students`
- Endpoint: `/partner/{partnerId}/sessions/limit-crossed`

### `CourseController.java`
Base Route: `/courses`
- Endpoint: `/{id}`
- Endpoint: `/{id}`
- Endpoint: `/{id}`

### `PartnerAccessRequestController.java`
Base Route: `/access-requests`
- Endpoint: `/request`
- Endpoint: `/{id}/status`
- Endpoint: `/{id}`
- Endpoint: `/vendor/{vendorId}`
- Endpoint: `/partner/{partnerId}`

### `MasterDataController.java`
Base Route: `/admin/master`
- Endpoint: `/batches`
- Endpoint: `/batches/{id}`
- Endpoint: `/batches`
- Endpoint: `/courses`
- Endpoint: `/courses/{id}`
- Endpoint: `/courses`
- Endpoint: `/partners`
- Endpoint: `/partners/{id}`
- Endpoint: `/partners`
- Endpoint: `/partner-access-requests`
- Endpoint: `/partner-access-requests/{id}`
- Endpoint: `/partner-access-requests`
- Endpoint: `/sessions`
- Endpoint: `/sessions/{id}`
- Endpoint: `/sessions`
- Endpoint: `/students`
- Endpoint: `/students/{id}`
- Endpoint: `/students`
- Endpoint: `/student-complaints`
- Endpoint: `/student-complaints/{id}`
- Endpoint: `/student-complaints`
- Endpoint: `/student-enrollments`
- Endpoint: `/student-enrollments/{id}`
- Endpoint: `/student-enrollments`
- Endpoint: `/users`
- Endpoint: `/users/{id}`
- Endpoint: `/users`
- Endpoint: `/user-sessions`
- Endpoint: `/user-sessions/{id}`
- Endpoint: `/user-sessions`
- Endpoint: `/vendors`
- Endpoint: `/vendors/{id}`
- Endpoint: `/vendors`
- Endpoint: `/student-session-logs`
- Endpoint: `/student-session-logs/{id}`
- Endpoint: `/student-session-logs`

### `BatchController.java`
Base Route: `/batches`
- Endpoint: `/{id}`
- Endpoint: `/{id}`
- Endpoint: `/{id}`

### `AuthController.java`
Base Route: `/auth`
- Endpoint: `/login`
- Endpoint: `/session`
- Endpoint: `/logout`
- Endpoint: `/send-otp`
- Endpoint: `/set-password`
- Endpoint: `/forgot-username`

### `OpenDataSyncController.java`
Base Route: `/api/v1/open/sync`
- Endpoint: `/student-data`
- Endpoint: `/complaints`
- Endpoint: `/courses`
- Endpoint: `/courses/{courseId}/batches`

### `SessionController.java`
Base Route: `/sessions`
- Endpoint: `/{id}`
- Endpoint: `/{id}`
- Endpoint: `/{id}`
- Endpoint: `/reorder`

### `ApiLogController.java`
Base Route: `/admin/logs`
- Endpoint: `/{id}`

