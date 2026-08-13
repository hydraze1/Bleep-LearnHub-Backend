# Bleep LearnHub - Production Optimization Report

Generated: 2026-08-13
Scope: Full backend audit — DTOs, controllers, services, API design

---

## Executive Summary

The current backend has **105 endpoints** across **18 controllers**, served by **~30 DTO classes** and **~20 service classes**. A deep audit reveals significant code duplication, DTO proliferation, and API surface bloat. This report identifies the root problems and proposes a production-grade redesign that would reduce the codebase by **40-50%** while improving maintainability, performance, and API quality.

---

## 1. Problems Identified

### 1.1 DTO Explosion — Identical Classes

The most glaring issue: multiple DTOs with **identical fields** exist purely because different controllers return them.

| DTO Pair | Fields | Difference |
|----------|--------|------------|
| `SessionDataDto` vs `SessionProfileResponseDto` | 15 fields | **ZERO** — they are byte-for-byte identical |
| `CourseDataDto` vs `CourseProfileResponseDto` | 6 core fields | Only `hasRequestedAccess` + `accessStatus` (2 extra) |
| `BatchDataDto` vs `BatchProfileResponseDto` | 8 core fields | Only `courseName`, `hasRequestedAccess`, `accessStatus` (3 extra) |

**Evidence from `SessionService.java` lines 141-181**: `mapToDataDto()` and `mapToProfileDto()` produce the exact same builder calls. Two methods, zero difference.

```java
// These two methods are IDENTICAL — same fields, same logic
private SessionDataDto mapToDataDto(Session session) { ... }        // line 141
private SessionProfileResponseDto mapToProfileDto(Session session) { ... } // line 162
```

### 1.2 Same Entity, 5+ Different Response Shapes

A single `Session` entity is returned in **6 different DTO shapes** across the API:

| DTO | Used By | Extra Fields |
|-----|---------|-------------|
| `SessionDataDto` | `GET /sessions` | — |
| `SessionProfileResponseDto` | `GET /sessions/{id}`, `POST /sessions` | (identical to above) |
| `PartnerSessionResponseDto` | Partner Portal | `studentCount`, `maxStudents`, `limitExceeded` |
| `PartnerAccessSessionDto` | Partner Access | `hasRequestedAccess`, `accessStatus`, conditional links |
| `PartnerCalendarSessionDto` | Calendar | Subset — no links, no access fields |
| `JoinLiveClassSessionResponseDto.SessionDto` | Open Sync | Subset — only liveLink, resourceLink |

This means a frontend developer working with sessions needs to understand **6 different TypeScript types** for the same entity.

### 1.3 Duplicate Mapping Code Across Services

Every service class contains its own private `mapToXxxDto()` methods. The same entity→DTO mapping logic is duplicated across:

- `CourseService.mapToDataDto()` / `mapToProfileDto()`
- `PartnerPortalService.mapToCourseDataDto()` / `mapToBatchDataDto()`
- `PartnerAccessService.mapToCourseDataDto()` / `mapToBatchDataDto()` / `mapToPartnerAccessSessionDto()`
- `SessionService.mapToDataDto()` / `mapToProfileDto()`

That's **~15 private mapper methods** doing essentially the same thing with slight variations.

### 1.4 Controller Bloat — MasterDataController

`MasterDataController.java` (143 lines) is a single controller handling **12 entities** with identical GET/DELETE patterns. Each entity gets:

```java
@GetMapping("/{entity}s")
public ResponseEntity<...> getAll{Entity}s(@RequestParam int page, @RequestParam int size, @RequestParam String search) { ... }

@DeleteMapping("/{entity}s/{id}")
public ResponseEntity<...> delete{Entity}(@PathVariable UUID id) { ... }

@DeleteMapping("/{entity}s")
public ResponseEntity<...> deleteAll{Entity}s() { ... }
```

This is 36 nearly-identical methods. A generic base controller would reduce this to zero.

### 1.5 No Pagination on Core List Endpoints

| Endpoint | Paginated? |
|----------|-----------|
| `GET /courses` | No — returns ALL |
| `GET /batches` | No — returns ALL |
| `GET /sessions` | No — returns ALL (with in-memory filtering!) |
| `GET /complaints` | No — returns ALL |
| `GET /partners` | No — returns ALL |
| `GET /vendors` | No — returns ALL |
| `GET /vendors/notifications` | Yes |
| `GET /partners/notifications` | Yes |
| `GET /admin/master/*` | Yes |

The `SessionService.getAllSessions()` method (line 110-130) fetches ALL sessions from DB then filters in Java streams — this will break at scale.

### 1.6 Inconsistent Auth Patterns

- Some controllers use class-level `@PreAuthorize`, others method-level
- Some use `Principal`, others use `Authentication`
- `PartnerController` has a `hasRole()` helper duplicated in `StudentComplaintController` and `DashboardController`
- `DashboardController` mixes dashboard + profile endpoints in one controller

### 1.7 Unused DTOs

`AuthResponseDto` — has `accessToken` field but the app uses cookie-based sessions, not JWT tokens. This DTO appears unused.

### 1.8 No API Versioning Strategy

Everything is under `/api/v1` but there's no mechanism to introduce v2 without duplicating the entire controller tree.

---

## 2. Production-Grade Target Architecture

### 2.1 Unified Response Envelope (Keep, but fix)

The `ApiResponse<T>` wrapper is good. Fix the `error` field — it currently holds `"200"` for success responses, which is confusing. Production pattern:

```java
// Current (broken):
.error("200")  // This is an HTTP status code, not an error

// Production:
// Remove the 'error' field from success responses entirely
// Use @JsonInclude(NON_NULL)
// Or rename to 'status' and use consistently
```

### 2.2 Single DTO Per Entity with Views/Projections

Instead of 6 session DTOs, use **one** with `@JsonView` or Jackson mixins:

```java
// ONE DTO — not six
public class SessionResponse {
    // Always present
    private UUID id;
    private UUID courseId;
    private UUID batchId;
    private String title;
    // ... core fields

    // Contextual — only when relevant
    @JsonInclude(NON_NULL)
    private String courseName;      // filled when context needs it
    @JsonInclude(NON_NULL)
    private String batchName;       // filled when context needs it
    @JsonInclude(NON_NULL)
    private Long studentCount;      // partner portal only
    @JsonInclude(NON_NULL)
    private Integer maxStudents;    // partner portal only
    @JsonInclude(NON_NULL)
    private Boolean limitExceeded;  // partner portal only
    @JsonInclude(NON_NULL)
    private String accessStatus;    // partner access only
    @JsonInclude(NON_NULL)
    private Boolean hasRequestedAccess; // partner access only
}
```

**Result**: 6 DTOs → 1 DTO. Frontend gets one TypeScript type. Null fields are simply absent from JSON.

### 2.3 Centralized Mapper Layer

Extract all entity→DTO mapping into a single `@Component`:

```java
@Component
public class EntityMapper {
    public SessionResponse toSessionResponse(Session s) { ... }
    public SessionResponse toSessionResponse(Session s, PartnerContext ctx) { ... } // overload with context
    public CourseResponse toCourseResponse(Course c) { ... }
    // ... all mappings in one place
}
```

**Result**: ~15 private mapper methods across 5 services → 1 class with ~8 methods. Changes to response shape happen in one file.

### 2.4 Generic CRUD Controller

Replace `MasterDataController` (143 lines, 36 methods) with a generic base:

```java
public abstract class AdminCrudController<T, ID> {
    @GetMapping
    public ResponseEntity<ApiResponse<Page<T>>> getAll(Pageable pageable, @RequestParam String search) {
        return ok(ApiResponse.success(service.getAll(pageable, search)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable ID id) {
        service.delete(id);
        return ok(ApiResponse.success("Deleted"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAll() {
        service.deleteAll();
        return ok(ApiResponse.success("All deleted"));
    }
}
```

Then each entity just extends:

```java
@RestController
@RequestMapping("/admin/master/batches")
public class AdminBatchController extends AdminCrudController<Batch, UUID> {
    public AdminBatchController(MasterDataService<Batch> service) { super(service); }
}
```

**Result**: 143 lines → ~10 lines per entity. 12 controllers become trivial.

### 2.5 Mandatory Pagination

Every list endpoint must return `Page<T>`, never `List<T>`. This is non-negotiable for production.

| Current | Production |
|---------|-----------|
| `GET /courses` → `List<CourseDataDto>` | `GET /courses?page=0&size=20` → `Page<CourseResponse>` |
| `GET /sessions` → `List<SessionDataDto>` | `GET /sessions?page=0&size=20&batchId=X` → `Page<SessionResponse>` |

Spring Data's `Pageable` argument resolver handles this automatically — just pass it through to the repository.

### 2.6 Consolidated Controller Map

| Current (18 controllers) | Production (10 controllers) | Rationale |
|--------------------------|------------------------------|-----------|
| `AuthController` | `AuthController` | Keep — distinct domain |
| `CourseController` | `CourseController` | Keep |
| `BatchController` | `BatchController` | Keep |
| `SessionController` | `SessionController` | Keep |
| `VendorController` | `VendorController` | Keep |
| `PartnerController` | `PartnerController` | Keep |
| `DashboardController` | `DashboardController` | Keep — merge profile endpoints here |
| `PartnerPortalController` | **MERGED** → `PartnerController` | Same entity, different view |
| `PartnerAccessController` | **MERGED** → `PartnerController` | Same entity, different view |
| `PartnerAccessRequestController` | `AccessRequestController` | Keep — distinct workflow |
| `StudentComplaintController` | `ComplaintController` | Keep |
| `VendorNotificationController` | `NotificationController` | **MERGED** — single controller for both sides |
| `PartnerNotificationController` | **MERGED** → `NotificationController` | Same entity, different role |
| `PartnerNotificationStreamController` | **MERGED** → `NotificationController` | SSE is just another endpoint |
| `OpenDataSyncController` | `OpenDataSyncController` | Keep — distinct auth boundary |
| `MasterDataController` | **REPLACED** → 12 tiny controllers extending `AdminCrudController` | Or one generic controller |
| `AuditLogController` | `AuditLogController` | Keep |
| `ApiLogController` | `ApiLogController` | Keep |

### 2.7 Endpoint Reduction

| Current Endpoint Group | Count | Production Count | Reduction |
|-----------------------|-------|-----------------|-----------|
| Partner Portal | 5 | 0 (merged into PartnerController) | -5 |
| Partner Access | 5 | 0 (merged into PartnerController) | -5 |
| Partner Notifications | 4 | 0 (merged into NotificationController) | -4 |
| Vendor Notifications | 4 | 0 (merged into NotificationController) | -4 |
| Partner SSE Stream | 1 | 0 (merged into NotificationController) | -1 |
| Admin Master Data | 24 | 24 (but via generic base) | 0 (but code reduced 90%) |
| **Total** | **105** | **~86** | **-19 endpoints** |

### 2.8 Proposed Final API Surface

```
/api/v1
├── /auth
│   ├── POST   /login
│   ├── GET    /session
│   ├── POST   /logout
│   ├── POST   /send-otp
│   ├── POST   /set-password
│   └── POST   /forgot-username
│
├── /courses
│   ├── GET    /                        (paginated, ?search=&sort=)
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   └── DELETE /{id}
│
├── /batches
│   ├── GET    /                        (paginated, ?courseId=)
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   └── DELETE /{id}
│
├── /sessions
│   ├── GET    /                        (paginated, ?courseId=&batchId=&type=&search=)
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   └── PUT    /reorder
│
├── /vendors
│   ├── GET    /                        (paginated)
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   └── DELETE /{id}
│
├── /partners
│   ├── GET    /                        (paginated, ?vendorId=)
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   │
│   ├── GET    /{id}/courses            (was partner-portal + partner-access)
│   ├── GET    /{id}/courses/{cid}/batches
│   ├── GET    /{id}/batches/{bid}/sessions
│   ├── GET    /{id}/students           (?batchId=&sessionId=)
│   ├── GET    /{id}/schedule           (?from=&to=)
│   └── GET    /{id}/sessions/limit-crossed
│
├── /access-requests
│   ├── GET    /                        (?vendorId=&partnerId=, paginated)
│   ├── POST   /                        (vendor creates)
│   ├── POST   /request                 (partner requests)
│   ├── PUT    /{id}/status
│   └── DELETE /{id}
│
├── /complaints
│   ├── GET    /                        (paginated, ?vendorId=&partnerId=)
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   └── DELETE /{id}
│
├── /notifications
│   ├── GET    /                        (paginated — vendor sees sent, partner sees received)
│   ├── POST   /                        (vendor creates)
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   ├── GET    /unread-count            (partner only)
│   ├── PATCH  /{id}/read               (partner only)
│   ├── PATCH  /read-all                (partner only)
│   └── GET    /stream                  (SSE — partner only)
│
├── /dashboard
│   ├── GET    /vendor/{id}
│   ├── GET    /partner/{id}
│   ├── GET    /vendor/{id}/profile
│   ├── PUT    /vendor/{id}/profile
│   ├── GET    /partner/{id}/profile
│   └── PUT    /partner/{id}/profile
│
├── /open/sync
│   ├── POST   /student-data
│   ├── POST   /complaints
│   ├── GET    /courses                 (?partnerId=)
│   ├── GET    /courses/{id}/batches    (?partnerId=)
│   └── GET    /session-details         (?sessionId=&partnerId=)
│
├── /admin
│   ├── /master/{entity}                (generic — 12 entities)
│   │   ├── GET    /
│   │   ├── DELETE /{id}
│   │   └── DELETE /
│   ├── /audit-logs
│   │   ├── GET    /
│   │   ├── DELETE /{id}
│   │   └── DELETE /
│   └── /logs
│       ├── GET    /
│       ├── DELETE /{id}
│       └── DELETE /
```

**Total: ~86 endpoints** (down from 105), organized under **10 controller groups** (down from 18).

---

## 3. DTO Consolidation Plan

### Before (30 DTOs)

```
response/
├── ApiResponse.java
├── AuthResponseDto.java              ← UNUSED — remove
├── CourseDataDto.java                ← merge into CourseResponse
├── CourseProfileResponseDto.java     ← merge into CourseResponse
├── BatchDataDto.java                 ← merge into BatchResponse
├── BatchProfileResponseDto.java      ← merge into BatchResponse
├── SessionDataDto.java               ← merge into SessionResponse
├── SessionProfileResponseDto.java    ← merge into SessionResponse (IDENTICAL)
├── PartnerSessionResponseDto.java    ← merge into SessionResponse
├── PartnerAccessSessionDto.java      ← merge into SessionResponse
├── PartnerCalendarSessionDto.java   ← merge into SessionResponse
├── PartnerCalendarDayDto.java        ← keep (aggregate)
├── JoinLiveClassSessionResponseDto.java ← keep (nested shape, different use case)
├── PartnerProfileResponseDto.java    ← merge into PartnerResponse
├── PartnerDataDto.java               ← merge into PartnerResponse
├── VendorProfileResponseDto.java     ← merge into VendorResponse
├── VendorDataDto.java                ← merge into VendorResponse
├── UserDataDto.java                  ← keep (auth-specific)
├── LoginResponseDto.java             ← keep (auth-specific)
├── DeviceDetailsDto.java             ← keep
├── PartnerDashboardResponseDto.java  ← keep (aggregate)
├── VendorDashboardResponseDto.java   ← keep (aggregate)
├── PartnerStudentResponseDto.java    ← keep (nested)
├── ComplaintResponseDto.java         ← keep
├── AccessRequestResponseDto.java     ← keep
├── StudentDataSyncResponseDto.java   ← keep (sync-specific)
│
request/  (8 DTOs — mostly fine, minor dedup possible)
└── ...
│
notification/  (3 DTOs)
├── PartnerNotificationResponseDto.java  ← merge into NotificationResponse
├── VendorNotificationResponseDto.java   ← merge into NotificationResponse
└── NotificationEventDto.java            ← keep (SSE event)
```

### After (~18 DTOs)

```
response/
├── ApiResponse.java                  ← fixed
├── SessionResponse.java              ← was 6 classes
├── CourseResponse.java               ← was 2 classes
├── BatchResponse.java                ← was 2 classes
├── PartnerResponse.java              ← was 2 classes
├── VendorResponse.java               ← was 2 classes
├── NotificationResponse.java         ← was 2 classes
├── UserResponse.java                 ← was UserDataDto
├── LoginResponse.java                ← keep
├── DeviceDetailsDto.java             ← keep
├── PartnerDashboardResponse.java     ← keep
├── VendorDashboardResponse.java      ← keep
├── PartnerStudentResponse.java       ← keep
├── ComplaintResponse.java            ← keep
├── AccessRequestResponse.java        ← keep
├── StudentDataSyncResponse.java      ← keep
├── JoinLiveClassSessionResponse.java ← keep
├── PartnerCalendarDayDto.java        ← keep
├── NotificationEventDto.java         ← keep
```

**Result: 30 DTOs → 19 DTOs (-37%)**

---

## 4. Service Layer Consolidation

### Before

| Service | Lines | Issue |
|---------|-------|-------|
| `CourseService` | 98 | Has its own mappers |
| `BatchService` | 117 | Has its own mappers |
| `SessionService` | 182 | Has duplicate mappers |
| `PartnerPortalService` | 268 | Has its own mappers, duplicates PartnerAccessService logic |
| `PartnerAccessService` | 448 | Has its own mappers, duplicates PartnerPortalService logic |
| `PartnerService` | ~150 | Has its own mappers |
| `VendorService` | ~100 | Has its own mappers |

### After

| Service | Lines | Change |
|---------|-------|--------|
| `EntityMapper` (new) | ~200 | All mapping logic centralized |
| `CourseService` | ~60 | Mappers removed |
| `BatchService` | ~70 | Mappers removed |
| `SessionService` | ~120 | Mappers removed, DB-level filtering added |
| `PartnerService` | ~300 | Absorbs PartnerPortalService + PartnerAccessService |
| `PartnerPortalService` | **DELETED** | Merged into PartnerService |
| `PartnerAccessService` | **DELETED** | Merged into PartnerService |

**Result: ~1,300 lines → ~750 lines (-42%)**

---

## 5. Implementation Priority

### Phase 1: Quick Wins (1-2 days)

1. **Delete `SessionProfileResponseDto`** — replace all usages with `SessionDataDto` (they're identical)
2. **Delete `AuthResponseDto`** — unused
3. **Merge `CourseDataDto` + `CourseProfileResponseDto`** → `CourseResponse` with `@JsonInclude(NON_NULL)`
4. **Merge `BatchDataDto` + `BatchProfileResponseDto`** → `BatchResponse` with `@JsonInclude(NON_NULL)`
5. **Add pagination** to `GET /courses`, `GET /batches`, `GET /sessions`, `GET /complaints`, `GET /partners`

### Phase 2: Structural (3-5 days)

6. **Create `EntityMapper`** component, migrate all mapping logic
7. **Merge session DTOs** — `PartnerSessionResponseDto`, `PartnerAccessSessionDto`, `PartnerCalendarSessionDto` → single `SessionResponse`
8. **Merge notification DTOs** — `PartnerNotificationResponseDto` + `VendorNotificationResponseDto` → `NotificationResponse`
9. **Merge `PartnerPortalService` + `PartnerAccessService`** → `PartnerService`
10. **Merge `PartnerPortalController` + `PartnerAccessController`** → `PartnerController`

### Phase 3: Architecture (3-5 days)

11. **Create `AdminCrudController<T>`** generic base, replace `MasterDataController`
12. **Merge notification controllers** — `VendorNotificationController` + `PartnerNotificationController` + `PartnerNotificationStreamController` → `NotificationController`
13. **Add proper API versioning** — URL prefix or header-based
14. **Add response filtering** — `?fields=id,title,scheduledDate` for sparse fieldsets

---

## 6. Before/After Summary

| Metric | Current | Target | Reduction |
|--------|---------|--------|-----------|
| Controllers | 18 | 10 | -44% |
| Endpoints | 105 | ~86 | -18% |
| Response DTOs | 30 | 19 | -37% |
| Service classes | ~20 | ~15 | -25% |
| Service code (lines) | ~1,300 | ~750 | -42% |
| Duplicate mapper methods | ~15 | 0 | -100% |
| Identical DTO pairs | 3 | 0 | -100% |
| Unpaginated list endpoints | 8 | 0 | -100% |
| Session DTO variants | 6 | 1 | -83% |

---

## 7. Key Principles Applied

1. **One entity, one response DTO** — contextual fields use `@JsonInclude(NON_NULL)`
2. **Centralized mapping** — one `EntityMapper`, not scattered private methods
3. **Generic CRUD** — don't write the same controller 12 times
4. **Pagination everywhere** — no unbounded `List<T>` returns
5. **Role-based views, not role-based endpoints** — same `/partners/{id}/courses` serves both vendor and partner views, differentiated by auth context
6. **Merge by entity, not by actor** — notifications belong together regardless of whether vendor or partner is calling
