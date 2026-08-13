# Bleep LearnHub — Optimization Implementation Strategy

Generated: 2026-08-13

---

## The Short Answer

**Do NOT create a new project.** Modify the existing one incrementally.

Here's why:

| Approach | Risk | Effort | Downtime |
|----------|------|--------|----------|
| New project (big-bang rewrite) | HIGH — you maintain two codebases, features diverge, cutover is a single point of failure | 3-4 weeks full-time | Hours of cutover + rollback risk |
| Incremental in existing project | LOW — each change is small, testable, reversible | Same total effort but spread over time | Zero — old and new coexist during migration |

A new project also means the frontend has to switch API base URLs atomically. If anything breaks, both old and new are down. Incremental lets you deploy one endpoint at a time.

---

## The Strategy: Strangler Fig Pattern

Don't rewrite. **Strangle** the old code by building the new alongside it, then removing the old once nothing calls it anymore.

```
Week 1-2:  Add new DTOs + mapper (old code untouched)
Week 2-3:  Migrate endpoints one controller at a time (old + new coexist)
Week 3-4:  Frontend switches to new endpoints
Week 4:    Delete old DTOs, old controllers, old service methods
```

At every step the app is deployable and working.

---

## Phase-by-Phase Implementation Plan

### Phase 0: Safety Net (Day 1 — 2 hours)

Before touching anything:

```
1. Ensure all existing endpoints have integration tests (or at least manual test checklist)
2. Add API response snapshot tests if possible
3. Tag current state: git tag v1-pre-optimization
```

This means you can always prove nothing broke.

---

### Phase 1: DTO Consolidation (Days 1-3)

**Goal**: Create the new unified DTOs. Old DTOs still exist. Nothing breaks.

#### Step 1.1: Create new DTOs in a new package

```
src/main/java/com/bleep/learnhub/dto/v2/
├── SessionResponse.java       ← replaces 6 session DTOs
├── CourseResponse.java        ← replaces 2 course DTOs
├── BatchResponse.java         ← replaces 2 batch DTOs
├── PartnerResponse.java       ← replaces 2 partner DTOs
├── VendorResponse.java        ← replaces 2 vendor DTOs
└── NotificationResponse.java  ← replaces 2 notification DTOs
```

**SessionResponse.java** — the key one, replacing 6 classes:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionResponse {
    // Core fields (always present)
    private UUID id;
    private UUID courseId;
    private UUID batchId;
    private String sessionType;
    private String title;
    private String subtitle;
    private String description;
    private Integer sequenceOrder;
    private String scheduledDate;
    private String scheduledTime;
    private String endTime;
    private String createdAt;
    private String updatedAt;

    // Links (present when caller has access)
    private String liveLink;
    private String recordedLink;
    private String resourceLink;

    // Contextual enrichment (null when not relevant → omitted from JSON)
    private String courseName;
    private String batchName;

    // Partner portal context
    private Long studentCount;
    private Integer maxStudents;
    private Boolean limitExceeded;

    // Partner access context
    private Boolean hasRequestedAccess;
    private String accessStatus;
}
```

**CourseResponse.java** — replaces CourseDataDto + CourseProfileResponseDto:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseResponse {
    private UUID id;
    private String title;
    private String subtitle;
    private String description;
    private String category;
    private String createdAt;
    private String updatedAt;

    // Partner access context only
    private Boolean hasRequestedAccess;
    private String accessStatus;
}
```

Same pattern for BatchResponse, PartnerResponse, VendorResponse, NotificationResponse.

#### Step 1.2: Create EntityMapper

```java
@Component
public class EntityMapper {

    // --- Session ---
    public SessionResponse toSessionResponse(Session s) {
        return SessionResponse.builder()
            .id(s.getId())
            .courseId(s.getCourseId())
            .batchId(s.getBatchId())
            .sessionType(s.getSessionType() != null ? s.getSessionType().name() : null)
            .title(s.getTitle())
            .subtitle(s.getSubtitle())
            .description(s.getDescription())
            .liveLink(s.getLiveLink())
            .recordedLink(s.getRecordedLink())
            .resourceLink(s.getResourceLink())
            .sequenceOrder(s.getSequenceOrder())
            .scheduledDate(s.getScheduledDate() != null ? s.getScheduledDate().toString() : null)
            .scheduledTime(s.getScheduledTime() != null ? s.getScheduledTime().toString() : null)
            .endTime(s.getEndTime() != null ? s.getEndTime().toString() : null)
            .createdAt(s.getCreatedAt() != null ? s.getCreatedAt().toString() : null)
            .updatedAt(s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : null)
            .build();
    }

    // Overload with enrichment
    public SessionResponse toSessionResponse(Session s, String courseName, String batchName) {
        SessionResponse r = toSessionResponse(s);
        r.setCourseName(courseName);
        r.setBatchName(batchName);
        return r;
    }

    // Overload with partner portal context
    public SessionResponse toSessionResponse(Session s, String courseName, String batchName,
                                              long studentCount, Integer maxStudents) {
        SessionResponse r = toSessionResponse(s, courseName, batchName);
        r.setStudentCount(studentCount);
        r.setMaxStudents(maxStudents);
        r.setLimitExceeded(maxStudents != null && studentCount > maxStudents);
        return r;
    }

    // --- Course ---
    public CourseResponse toCourseResponse(Course c) { ... }
    public CourseResponse toCourseResponse(Course c, AccessRequestStatus status) { ... }

    // --- Batch ---
    public BatchResponse toBatchResponse(Batch b) { ... }
    public BatchResponse toBatchResponse(Batch b, String courseName) { ... }
    public BatchResponse toBatchResponse(Batch b, String courseName, AccessRequestStatus status) { ... }

    // --- Partner ---
    public PartnerResponse toPartnerResponse(Partner p) { ... }

    // --- Vendor ---
    public VendorResponse toVendorResponse(Vendor v) { ... }

    // --- Notification ---
    public NotificationResponse toNotificationResponse(Notification n, NotificationRecipient r) { ... }
}
```

**At this point**: New DTOs and mapper exist. Old code is completely untouched. App builds and runs exactly as before. Deploy this — zero risk.

---

### Phase 2: Migrate Controllers One at a Time (Days 3-7)

**Rule**: Migrate one controller per PR. Deploy each independently. If anything breaks, revert that one PR.

#### Migration order (least risky first):

| Order | Controller | Risk | Reason |
|-------|-----------|------|--------|
| 1 | `CourseController` | Very Low | Simple CRUD, no cross-entity logic |
| 2 | `BatchController` | Very Low | Simple CRUD |
| 3 | `SessionController` | Low | Has reorder logic but straightforward |
| 4 | `VendorController` | Low | SUPER_ADMIN only, low traffic |
| 5 | `PartnerController` | Medium | Has vendor-scoping logic |
| 6 | `ComplaintController` | Low | Simple CRUD |
| 7 | `AccessRequestController` | Medium | Has workflow state machine |
| 8 | `DashboardController` | Medium | Aggregate queries |
| 9 | `NotificationController` | Medium | SSE stream needs care |
| 10 | `OpenDataSyncController` | High | No auth — external systems depend on exact response shape |

#### How to migrate one controller:

**Step A**: Add new endpoint methods alongside old ones (different URL temporarily):

```java
@RestController
@RequestMapping("/courses")
public class CourseController {

    // OLD — keep working
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseDataDto>>> getAllCourses() {
        // existing code, untouched
    }

    // NEW — add alongside
    @GetMapping("/v2")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getAllCoursesV2(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CourseResponse> page = courseService.getAllCourses(pageable)
            .map(entityMapper::toCourseResponse);
        return ResponseEntity.ok(ApiResponse.success(page, "Courses retrieved"));
    }
}
```

**Step B**: Update frontend to call `/courses/v2` (or use a feature flag).

**Step C**: Once frontend is fully on v2, remove the old method and old DTO.

**Step D**: Rename `/v2` → `/` (or keep v2 and add URL rewrite in Traefik/nginx).

#### For the big merges (PartnerPortal + PartnerAccess → PartnerController):

Don't merge controllers immediately. First, create the new unified endpoints:

```java
// NEW unified partner endpoints
@GetMapping("/{id}/courses")
public ResponseEntity<ApiResponse<Page<CourseResponse>>> getPartnerCourses(
        @PathVariable UUID id, Authentication auth) {
    // Combines logic from both PartnerPortalService and PartnerAccessService
    // Role-aware: VENDOR sees all, PARTNER sees only their own
}
```

Keep old `/partner-portal/*` and `/partner-access/*` endpoints working. Add the new ones. Let frontend migrate. Then delete old controllers.

---

### Phase 3: Service Layer Cleanup (Days 7-10)

Only after all controllers use the new DTOs:

1. **Delete old DTOs** — `SessionDataDto`, `SessionProfileResponseDto`, `CourseDataDto`, `CourseProfileResponseDto`, `BatchDataDto`, `BatchProfileResponseDto`, `PartnerSessionResponseDto`, `PartnerAccessSessionDto`, `PartnerCalendarSessionDto`, `PartnerProfileResponseDto`, `PartnerDataDto`, `VendorProfileResponseDto`, `VendorDataDto`, `PartnerNotificationResponseDto`, `VendorNotificationResponseDto`, `AuthResponseDto`

2. **Delete old private mapper methods** from services — they're no longer called

3. **Merge `PartnerPortalService` + `PartnerAccessService`** → `PartnerService`:
   - Move all methods into `PartnerService`
   - Update `PartnerController` to use `PartnerService` directly
   - Delete the two old service classes

4. **Create `AdminCrudController<T>`** and replace `MasterDataController`:
   - This is safe because it's SUPER_ADMIN only and the response shape doesn't change (it returns raw entities)

---

### Phase 4: Pagination & Performance (Days 10-12)

Now that DTOs are unified, add proper pagination:

```java
// Before (SessionService):
public List<SessionDataDto> getAllSessions(UUID courseId, UUID batchId, ...) {
    List<Session> sessions = sessionRepository.findAll(); // FETCHES ALL
    return sessions.stream()
        .filter(...)  // IN-MEMORY FILTER
        .map(...)
        .collect(...);
}

// After:
public Page<SessionResponse> getAllSessions(UUID courseId, UUID batchId, String type,
                                             String search, Pageable pageable) {
    Specification<Session> spec = Specification.where(null);
    if (courseId != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("courseId"), courseId));
    if (batchId != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("batchId"), batchId));
    if (type != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("sessionType"), SessionType.valueOf(type)));
    if (search != null) spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"));

    return sessionRepository.findAll(spec, pageable)
        .map(entityMapper::toSessionResponse);
}
```

This moves filtering from Java memory to the database — critical for production.

---

### Phase 5: Frontend Migration (Parallel with Phase 2-4)

The frontend migration is actually easier than the backend:

1. **Create new TypeScript types** matching the unified DTOs:

```typescript
// Before: 6 types for sessions
type SessionData = { ... }
type SessionProfileData = { ... }
type PartnerSessionData = { ... }
type PartnerAccessSessionData = { ... }
type PartnerCalendarSessionData = { ... }
type JoinLiveClassSessionData = { ... }

// After: 1 type
type SessionResponse = {
  id?: string;
  courseId?: string;
  batchId?: string;
  sessionType?: string;
  title?: string;
  // ... core fields
  courseName?: string;       // optional — only present in some contexts
  batchName?: string;        // optional
  studentCount?: number;     // optional
  maxStudents?: number;      // optional
  limitExceeded?: boolean;   // optional
  hasRequestedAccess?: boolean; // optional
  accessStatus?: string;     // optional
  liveLink?: string;         // optional — null when no access
  recordedLink?: string;     // optional
  resourceLink?: string;     // optional
}
```

2. **Update hooks one domain at a time** — start with courses (simplest), then batches, then sessions.

3. **Remove old types** once all hooks in a domain are migrated.

---

## What You Can Do RIGHT NOW (Today, Zero Risk)

These changes are safe to deploy immediately:

### 1. Delete `SessionProfileResponseDto` — replace with `SessionDataDto`

They are identical. Just change the import in `SessionController` and `SessionService`:

```java
// In SessionController.java:
// Change: SessionProfileResponseDto → SessionDataDto
public ResponseEntity<ApiResponse<SessionDataDto>> createSession(...)
public ResponseEntity<ApiResponse<SessionDataDto>> getSessionById(...)

// In SessionService.java:
// Delete mapToProfileDto() method
// Change all callers to use mapToDataDto()
```

This removes one class, one duplicate mapper method, zero behavioral change.

### 2. Delete `AuthResponseDto`

It's unused. Just delete the file.

### 3. Add `@JsonInclude(NON_NULL)` to `CourseDataDto` and `BatchDataDto`

Then `CourseProfileResponseDto` becomes redundant — `CourseDataDto` with null `hasRequestedAccess`/`accessStatus` is identical. Same for batches.

### 4. Add pagination to `GET /courses` and `GET /batches`

These are the simplest — just change the repository call:

```java
// CourseService.java — add this method:
public Page<CourseDataDto> getAllCourses(Pageable pageable) {
    return courseRepository.findAll(pageable).map(this::mapToDataDto);
}
```

Add `Pageable` parameter to the controller. Frontend adds `?page=0&size=20`. Old behavior preserved if no params passed (just default to large page size during transition).

---

## Summary: The Path Forward

```
NOW (today):
  ├── Delete SessionProfileResponseDto (identical to SessionDataDto)
  ├── Delete AuthResponseDto (unused)
  ├── Add @JsonInclude(NON_NULL) to CourseDataDto, BatchDataDto
  └── Add pagination to GET /courses, GET /batches

WEEK 1:
  ├── Create dto/v2/ package with unified DTOs
  ├── Create EntityMapper component
  └── Migrate CourseController + BatchController to v2 DTOs

WEEK 2:
  ├── Migrate SessionController, VendorController, PartnerController
  ├── Frontend switches to v2 types for migrated domains
  └── Delete old DTOs for migrated domains

WEEK 3:
  ├── Merge PartnerPortal + PartnerAccess → PartnerController
  ├── Merge notification controllers → NotificationController
  ├── Create AdminCrudController<T>, replace MasterDataController
  └── Add DB-level filtering to SessionService

WEEK 4:
  ├── Delete old service classes (PartnerPortalService, PartnerAccessService)
  ├── Delete all old DTOs
  ├── Final frontend cleanup
  └── Performance testing
```

**Key rule**: At every commit, the app builds, passes tests, and deploys. No big-bang. No new project. No downtime.
