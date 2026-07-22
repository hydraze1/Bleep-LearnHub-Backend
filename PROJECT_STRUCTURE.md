# Bleep LearnHub Backend - Project Structure & Quality Report

This document provides a comprehensive overview of the `bleep-learnhub-backend` directory structure, the purpose of each folder, a file-by-file description, and a detailed audit of issues found in the codebase.

---

## 🛠️ Codebase Quality & Compilation Audit

Upon analyzing the files and performing a test compilation, **the codebase currently fails to compile with 98 compilation errors.** 

Below is a detailed report of the issues that need to be resolved to make the project build successfully:

### 1. Missing Classes & Empty Source Files
*   **`UserRepository.java`**: This file is completely empty (0 bytes). As a result, the compiler cannot find the class/interface `com.bleep.learnhub.repository.UserRepository`, causing compilation failures in `AuthService` and `VendorService`.
*   **`AdminService.java`**: Injected into `AdminController` but the class does not exist in `com.bleep.learnhub.service`.
*   **`RedisService.java`**: Injected into `AuthService` and `JwtAuthenticationFilter` to manage active sessions and OTPs, but the class does not exist in `com.bleep.learnhub.service`.
*   **`JwtUtil.java`**: Injected into `AuthService` but does not exist. (The class managing JWT token extraction and generation is instead named `JwtService`).

### 2. Missing Maven Dependencies in `pom.xml`
*   **Java JWT (JJWT)**: `JwtService.java` imports classes from `io.jsonwebtoken.*` (like `Jwts`, `Claims`, `Decoders`, `Keys`), but the dependencies for JWT are not declared in `pom.xml`.
*   **Swagger / OpenAPI**: `OpenApiConfig.java` imports classes from `io.swagger.v3.oas.models.*` but the OpenAPI dependencies (e.g., `springdoc-openapi-starter-webmvc-ui`) are missing from `pom.xml`.

### 3. Package & Directory Mismatches
*   **`AccountStatus.java` & `Role.java`**: These enums are located in the directory `src/main/java/com/bleep/learnhub/enums` but declare their package as `package com.bleep.learnhub.entity.enums;`. Java requires package declarations to match their actual folder hierarchy.
*   **`JwtAuthenticationFilter.java`**: This file is located under `security/` but `SecurityConfig.java` attempts to import it as `com.bleep.learnhub.filter.JwtAuthFilter;`. The import statement and class name reference must be updated.
*   **DTO Requests**: Classes like `VendorCreateDto`, `LoginRequestDto`, `SetPasswordDto`, and `PartnerCreateDto` are placed under the `com.bleep.learnhub.dto.request` package, but the controllers import them from the parent package `com.bleep.learnhub.dto` (e.g. `import com.bleep.learnhub.dto.LoginRequestDto;`), leading to import errors.

### 4. Missing Infrastructure Beans
*   **`UserDetailsService`**: The filter `JwtAuthenticationFilter` injects `UserDetailsService`, but there is no implementation class or `@Bean` configured for it. Without a `UserDetailsService` bean, Spring Security will fail dependency injection and initialization.

---

## 📁 Directory & File Index

Here is the directory structure under `src/main/java/com/bleep/learnhub/` and descriptions of what each folder and file does:

### 1. `config`
**Purpose**: Contains Java-based configuration classes for initializing various frameworks, libraries, and Spring Beans (e.g., Security, Redis, OpenAPI, Mail).

*   **[MailConfig.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/config/MailConfig.java)**:
    Configures and instantiates the `JavaMailSender` bean using properties from `application.properties` to enable email-sending functionality.
*   **[OpenApiConfig.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/config/OpenApiConfig.java)**:
    Configures Swagger/OpenAPI documentation, defining a global JWT bearer security scheme to authorize API requests during testing.
*   **[RedisConfig.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/config/RedisConfig.java)**:
    Sets up the `RedisTemplate<String, Object>` configuration, utilizing string serialization for Redis keys and Jackson JSON serialization for stored session/OTP values.
*   **[SecurityConfig.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/config/SecurityConfig.java)**:
    Sets up Spring Security, configuring the application as a stateless API session, authorizing public auth endpoints (like `/api/v1/auth/**`), and registering the custom JWT authentication filter.

---

### 2. `controller`
**Purpose**: Exposes the REST API endpoints and handles incoming HTTP requests. Delegates business logic execution to services and returns standard HTTP responses.

*   **[AdminController.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/controller/AdminController.java)**:
    Exposes endpoints restricted to `SUPER_ADMIN` users, allowing them to create new Vendor accounts (which sends onboarding setup emails) and block Vendors.
*   **[AuthController.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/controller/AuthController.java)**:
    Handles authentication-related HTTP operations: sending OTPs to users, validating OTPs to set passwords, handling login requests (generating JWTs), and logging out (invalidating sessions).
*   **[PartnerController.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/controller/PartnerController.java)**:
    Exposes endpoints restricted to `PARTNER` users, allowing them to fetch their business profile details and retrieve courses allocated by their parent Vendor.
*   **[VendorController.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/controller/VendorController.java)**:
    Exposes endpoints restricted to `VENDOR` users, allowing them to onboard/create Partner accounts under their tenancy and retrieve their list of partners.

---

### 3. `dto`
**Purpose**: Data Transfer Objects (DTOs) used to define the payloads for incoming request payloads and outgoing API response structures.

*   **`dto/request`**:
    *   **[LoginRequestDto.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/dto/request/LoginRequestDto.java)**: DTO holding login credentials (`username` and `password`).
    *   **[PartnerCreateDto.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/dto/request/PartnerCreateDto.java)**: Payload for creating a Partner, containing fields like `username`, `email`, `companyName`, and `phone`.
    *   **[SetPasswordDto.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/dto/request/SetPasswordDto.java)**: Payload for verifying OTP and setting the initial account password.
    *   **[VendorCreateDto.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/dto/request/VendorCreateDto.java)**: Payload for creating a Vendor account, containing `username`, `email`, and `companyName`.
*   **`dto/response`**:
    *   **[AuthResponseDto.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/dto/response/AuthResponseDto.java)**: Payload returned upon successful login, including the JWT access token, user role, and username.
    *   **[PartnerProfileResponseDto.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/dto/response/PartnerProfileResponseDto.java)**: Payload containing detailed Partner profile information, including references to its parent Vendor.
    *   **[VendorProfileResponseDto.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/dto/response/VendorProfileResponseDto.java)**: Detailed Vendor profile data returned to client applications.

---

### 4. `entity`
**Purpose**: Contains JPA entities that map directly to database tables in PostgreSQL.

*   **[AuditLog.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/entity/AuditLog.java)**:
    Maps to `audit_logs` table. Stores records of critical system events (e.g., password resets, onboarding events) along with user ID, IP address, and JSON payloads.
*   **[Partner.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/entity/Partner.java)**:
    Maps to `partners` table. Represents partner profile data, which has a one-to-one mapping with the `User` and a many-to-one mapping with its parent `Vendor`.
*   **[User.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/entity/User.java)**:
    Maps to `users` table. Holds credentials, status, role, email verification details, and registration metadata.
*   **[UserSession.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/entity/UserSession.java)**:
    Maps to `user_sessions` table. Tracks user login history, IP address, browser, OS, and token validity identifier (JTI).
*   **[Vendor.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/entity/Vendor.java)**:
    Maps to `vendors` table. Represents the Vendor entity associated with a `User` account.

---

### 5. `enums`
**Purpose**: Stores Java Enum types used to define finite sets of values for database constraints and business roles.

*   **[AccountStatus.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/enums/AccountStatus.java)**:
    Enum for user accounts status: `PENDING_SETUP`, `ACTIVE`, `BLOCKED`.
*   **[Role.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/enums/Role.java)**:
    Enum defining permission groups: `SUPER_ADMIN`, `VENDOR`, `PARTNER`.

---

### 6. `exception`
**Purpose**: Implements custom exception classes and global handler routines to normalize API error messages.

*   **[BusinessException.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/exception/BusinessException.java)**:
    Exception thrown when business logic rules are violated (mapped to HTTP 400 Bad Request).
*   **[ResourceNotFoundException.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/exception/ResourceNotFoundException.java)**:
    Exception thrown when requested data is not found (mapped to HTTP 404 Not Found).
*   **[GlobalExceptionHandler.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/exception/GlobalExceptionHandler.java)**:
    A `@RestControllerAdvice` that intercepts all controller-thrown exceptions (Validation, BadCredentials, AccessDenied, BusinessException, ResourceNotFoundException) and formats them into standardized JSON error payloads.

---

### 7. `repository`
**Purpose**: Spring Data JPA repositories responsible for executing database queries on PostgreSQL.

*   **[UserRepository.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/repository/UserRepository.java)**:
    *(Currently Empty)* Meant to handle query operations on the `User` entity.
*   **[UserSessionRepository.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/repository/UserSessionRepository.java)**:
    Executes database queries for user session entries and manages session revoking.
*   **[VendorRepository.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/repository/VendorRepository.java)**:
    Handles data access for Vendor records, including fetching profiles by usernames.
*   **[PartnerRepository.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/repository/PartnerRepository.java)**:
    Handles data access for Partner records, finding profiles by user credentials or parent Vendor IDs.

---

### 8. `security`
**Purpose**: Integrates JWT authentication filters and encapsulates identity claims parsing.

*   **[JwtAuthenticationFilter.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/security/JwtAuthenticationFilter.java)**:
    A custom request interceptor that extracts the authorization header token, verifies that the session is active in Redis, loads User details, and registers the authentication into the security context.
*   **[JwtService.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/security/JwtService.java)**:
    Responsible for generating, building, signing, and parsing claims (like subject, role, JTI expiration) of JWT tokens.
*   **[UserPrincipal.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/security/UserPrincipal.java)**:
    Wrapper implementing Spring Security's `UserDetails` contract on top of the custom `User` entity.

---

### 9. `service`
**Purpose**: Encapsulates core business logic functions of the SaaS platform.

*   **[AuthService.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/service/AuthService.java)**:
    Coordinates OTP generation and messaging, password initialization, validation, login processing (session creation), and logout operations.
*   **[EmailService.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/service/EmailService.java)**:
    Sends asynchronous emails (such as welcome notifications and OTP authorization keys) via SMTP.
*   **[PartnerService.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/service/PartnerService.java)**:
    Retrieves and maps Partner profile database tables to DTO responses.
*   **[VendorService.java](file:///home/hydraze1/Desktop/code/github/Bleep-LearnHub-Backend/src/main/java/com/bleep/learnhub/service/VendorService.java)**:
    Manages partner onboarding workflows (creating user credentials and partner profiles) and lists partners registered under a particular vendor.

---

### 10. Empty Folders (Awaiting Future Implementation)
*   `filter`: Intended for custom servlet filters.
*   `mapper`: For mapping DTOs to Entities.
*   `util`: For miscellaneous utility functions.
