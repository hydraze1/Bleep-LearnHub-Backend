# Project Structure and Details

## config

### AdminSeeder.java
```java
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private String adminUsername;
    private String adminEmail;
    private String adminPassword;
    public void run(String... args) {
``n
### ApiLoggingFilter.java
```java
public class ApiLoggingFilter extends OncePerRequestFilter {
    private final ApiLogRepository apiLogRepository;
    private final RedisService redisService;
    private final ExecutorService logExecutor = Executors.newSingleThreadExecutor();
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    private String getPayload(byte[] buf, String characterEncoding) {
    private String truncate(String payload, int maxLength) {
    private String extractMaskedSessionId(HttpServletRequest request) {
``n
### LayerLoggingAspect.java
```java
public class LayerLoggingAspect {
``n
### MailConfig.java
```java
public class MailConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    public JavaMailSender javaMailSender() {
``n
### MasterLoggingFilter.java
```java
public class MasterLoggingFilter extends OncePerRequestFilter {
    private boolean verboseLogging;
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    private void logRequestBody(ContentCachingRequestWrapper request) {
``n
### OpenApiConfig.java
```java
public class OpenApiConfig {
    public OpenAPI customOpenAPI() {
``n
### RedisConfig.java
```java
public class RedisConfig {
    public ObjectMapper objectMapper() {
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
``n
### RequestTraceFilter.java
```java
public class RequestTraceFilter {
``n
### SecurityConfig.java
```java
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ApiLogRepository apiLogRepository;
    private final RedisService redisService;
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
    public AccessDeniedHandler customAccessDeniedHandler() {
    public PasswordEncoder passwordEncoder() {
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
``n
## controller

### ApiLogController.java
```java
public class ApiLogController {
    private final ApiLogService apiLogService;
    public ResponseEntity<ApiResponse<Page<ApiLog>>> getAllLogs(
    public ResponseEntity<ApiResponse<Void>> deleteLog(@PathVariable UUID id) {
    public ResponseEntity<ApiResponse<Void>> deleteAllLogs() {
``n
### AuthController.java
```java
public class AuthController {
    private final AuthService authService;
    private final CookieService cookieService;
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
    public ResponseEntity<ApiResponse<LoginResponseDto>> session(HttpServletRequest request) {
    public ResponseEntity<ApiResponse<Void>> logout(
    public ResponseEntity<ApiResponse<Void>> sendOtp(
    public ResponseEntity<ApiResponse<Void>> setPassword(
    public ResponseEntity<ApiResponse<Void>> forgotUsername(
    public ResponseEntity<ApiResponse<List<UserDataDto>>> getUserList() {
    private String extractCookie(HttpServletRequest request, String cookieName) {
``n
### BatchController.java
```java
public class BatchController {
    private final BatchService batchService;
    public ResponseEntity<ApiResponse<BatchProfileResponseDto>> createBatch(@Valid @RequestBody BatchCreateDto dto) {
    public ResponseEntity<ApiResponse<Void>> updateBatch(
    public ResponseEntity<ApiResponse<Void>> deleteBatch(@PathVariable UUID id) {
    public ResponseEntity<ApiResponse<List<BatchDataDto>>> getAllBatches(
    public ResponseEntity<ApiResponse<BatchProfileResponseDto>> getBatchById(@PathVariable UUID id) {
``n
### CourseController.java
```java
public class CourseController {
    private final CourseService courseService;
    public ResponseEntity<ApiResponse<CourseProfileResponseDto>> createCourse(@Valid @RequestBody CourseCreateDto dto) {
    public ResponseEntity<ApiResponse<Void>> updateCourse(
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID id) {
    public ResponseEntity<ApiResponse<List<CourseDataDto>>> getAllCourses() {
    public ResponseEntity<ApiResponse<CourseProfileResponseDto>> getCourseById(@PathVariable UUID id) {
``n
### DashboardController.java
```java
public class DashboardController {
    private final DashboardService dashboardService;
    private final VendorRepository vendorRepository;
    private final PartnerRepository partnerRepository;
    private final com.bleep.learnhub.service.VendorService vendorService;
    private final com.bleep.learnhub.service.PartnerService partnerService;
    public ResponseEntity<ApiResponse<VendorDashboardResponseDto>> getVendorDashboard(
    public ResponseEntity<ApiResponse<PartnerDashboardResponseDto>> getPartnerDashboard(
    public ResponseEntity<ApiResponse<VendorProfileResponseDto>> getVendorProfile(
    public ResponseEntity<ApiResponse<Void>> updateVendorProfile(
    public ResponseEntity<ApiResponse<PartnerProfileResponseDto>> getPartnerProfile(
    public ResponseEntity<ApiResponse<Void>> updatePartnerProfile(
``n
### OpenDataSyncController.java
```java
public class OpenDataSyncController {
    private final StudentDataSyncService studentDataSyncService;
    private final StudentComplaintService studentComplaintService;
    private final PartnerPortalService partnerPortalService;
    public ResponseEntity<com.bleep.learnhub.dto.response.ApiResponse<StudentDataSyncResponseDto>> syncStudentData(@RequestBody StudentDataSyncRequest request) {
    public ResponseEntity<com.bleep.learnhub.dto.response.ApiResponse<Void>> createComplaint(@Valid @RequestBody ComplaintCreateDto dto) {
    public ResponseEntity<com.bleep.learnhub.dto.response.ApiResponse<List<CourseDataDto>>> getCourses(@RequestParam UUID partnerId) {
    public ResponseEntity<com.bleep.learnhub.dto.response.ApiResponse<List<BatchDataDto>>> getBatchesByCourse(@PathVariable UUID courseId, @RequestParam UUID partnerId) {
``n
### PartnerAccessController.java
```java
public class PartnerAccessController {
    private final PartnerAccessService partnerAccessService;
    public ResponseEntity<ApiResponse<List<CourseDataDto>>> getCourses(
    public ResponseEntity<ApiResponse<List<BatchDataDto>>> getBatches(
    public ResponseEntity<ApiResponse<List<PartnerAccessSessionDto>>> getSessions(
    public ResponseEntity<ApiResponse<List<PartnerCalendarDayDto>>> getSchedule(
``n
### PartnerAccessRequestController.java
```java
public class PartnerAccessRequestController {
    private final PartnerAccessRequestService accessRequestService;
    public ResponseEntity<ApiResponse<Void>> createAccessRequest(
    public ResponseEntity<ApiResponse<Void>> requestAccess(
    public ResponseEntity<ApiResponse<Void>> updateAccessStatus(
    public ResponseEntity<ApiResponse<Void>> deleteAccessRecord(@PathVariable UUID id) {
    public ResponseEntity<ApiResponse<List<AccessRequestResponseDto>>> getVendorRequests(@PathVariable UUID vendorId) {
    public ResponseEntity<ApiResponse<List<AccessRequestResponseDto>>> getPartnerRequests(@PathVariable UUID partnerId) {
``n
### PartnerController.java
```java
public class PartnerController {
    private final PartnerService partnerService;
    public ResponseEntity<ApiResponse<Void>> createPartner(
    public ResponseEntity<ApiResponse<List<PartnerProfileResponseDto>>> getAllPartners(
    public ResponseEntity<ApiResponse<List<PartnerProfileResponseDto>>> getPartnersByVendorId(
    public ResponseEntity<ApiResponse<PartnerProfileResponseDto>> getPartnerById(
    public ResponseEntity<ApiResponse<Void>> updatePartner(
    public ResponseEntity<ApiResponse<Void>> deletePartner(
    private boolean hasRole(Authentication authentication, String role) {
``n
### PartnerPortalController.java
```java
public class PartnerPortalController {
    private final PartnerPortalService partnerPortalService;
    public ResponseEntity<ApiResponse<List<CourseDataDto>>> getCourses(
    public ResponseEntity<ApiResponse<List<BatchDataDto>>> getBatches(
    public ResponseEntity<ApiResponse<List<PartnerSessionResponseDto>>> getSessions(
    public ResponseEntity<ApiResponse<List<PartnerStudentResponseDto>>> getStudents(
    public ResponseEntity<ApiResponse<List<PartnerSessionResponseDto>>> getLimitCrossedSessions(
``n
### SessionController.java
```java
public class SessionController {
    private final SessionService sessionService;
    public ResponseEntity<ApiResponse<SessionProfileResponseDto>> createSession(@Valid @RequestBody SessionCreateDto dto) {
    public ResponseEntity<ApiResponse<Void>> updateSession(
    public ResponseEntity<ApiResponse<Void>> deleteSession(@PathVariable UUID id) {
    public ResponseEntity<ApiResponse<List<SessionDataDto>>> getAllSessions(
    public ResponseEntity<ApiResponse<SessionProfileResponseDto>> getSessionById(@PathVariable UUID id) {
    public ResponseEntity<ApiResponse<Void>> reorderSessions(
``n
### StudentComplaintController.java
```java
public class StudentComplaintController {
    private final StudentComplaintService complaintService;
    public ResponseEntity<ApiResponse<Void>> createComplaint(@Valid @RequestBody ComplaintCreateDto dto) {
    public ResponseEntity<ApiResponse<Void>> updateComplaint(
    public ResponseEntity<ApiResponse<Void>> deleteComplaint(@PathVariable UUID id) {
    public ResponseEntity<ApiResponse<List<ComplaintResponseDto>>> getComplaints(
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> getComplaintById(@PathVariable UUID id) {
    private boolean hasRole(Authentication authentication, String role) {
``n
### VendorController.java
```java
public class VendorController {
    private final VendorService vendorService;
    public ResponseEntity<ApiResponse<Void>> createVendor(@Valid @RequestBody VendorCreateDto dto) {
    public ResponseEntity<ApiResponse<List<VendorProfileResponseDto>>> getAllVendors() {
    public ResponseEntity<ApiResponse<VendorProfileResponseDto>> getVendorById(@PathVariable UUID id) {
    public ResponseEntity<ApiResponse<Void>> updateVendor(@PathVariable UUID id, @Valid @RequestBody VendorUpdateDto dto) {
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable UUID id) {
``n
## dto

### OtpSessionData.java
```java
public class OtpSessionData {
    private String username;
    private String email;
    private String otp;
``n
### StudentDataSyncRequest.java
```java
public class StudentDataSyncRequest {
    private UUID studentId;
    private UUID partnerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String college;
    private String branch;
    private String academicYear;
    private UUID enrollmentId;
    private UUID courseId;
    private UUID enrollmentBatchId;
    private String enrollmentCourseName;
    private String enrollmentBatchName;
``n
### AccessStatusUpdateDto.java
```java
public class AccessStatusUpdateDto {
    private AccessRequestStatus status;
    private String responseNote;
    private Integer maxStudents;
    private java.util.UUID courseId;
    private String courseName;
    private java.util.UUID batchId;
    private String batchName;
``n
### BatchCreateDto.java
```java
public class BatchCreateDto {
    private UUID courseId;
    private String title;
    private String subtitle;
    private String description;
    private LocalDate startingDate;
    private LocalDate endingDate;
``n
### BatchUpdateDto.java
```java
public class BatchUpdateDto {
    private String title;
    private String subtitle;
    private String description;
    private LocalDate startingDate;
    private LocalDate endingDate;
``n
### ComplaintCreateDto.java
```java
public class ComplaintCreateDto {
    private UUID studentId;
    private UUID partnerId;
    private UUID vendorId;
    private UUID courseId;
    private UUID batchId;
    private String studentName;
    private String email;
    private String phoneNumber;
    private String college;
    private String branch;
    private String academicYear;
    private String courseName;
    private String batchName;
    private String complaintTitle;
``n
### ComplaintUpdateDto.java
```java
public class ComplaintUpdateDto {
    private ComplaintStatus status;
    private String vendorRemark;
    private String partnerRemark;
``n
### CourseCreateDto.java
```java
public class CourseCreateDto {
    private String title;
    private String subtitle;
    private String description;
    private String category;
``n
### CourseUpdateDto.java
```java
public class CourseUpdateDto {
    private String title;
    private String subtitle;
    private String description;
    private String category;
``n
### ForgotUsernameRequestDto.java
```java
public class ForgotUsernameRequestDto {
    private String email;
``n
### LoginRequestDto.java
```java
public class LoginRequestDto {
    private String username;
    private String password;
``n
### PartnerAccessRequestCreateDto.java
```java
public class PartnerAccessRequestCreateDto {
    private UUID courseId;
    private String courseName;
    private UUID batchId;
    private String batchName;
    private UUID partnerId;
    private String partnerName;
    private Integer maxStudents;
    private String note; // request note (optional)
``n
### PartnerCreateDto.java
```java
public class PartnerCreateDto {
    private String username;
    private String email;
    private String companyName;
    private String phone;
    private String description;
    private String vendorId;
``n
### PartnerUpdateDto.java
```java
public class PartnerUpdateDto {
    private String companyName;
    private String phone;
    private String description;
    private Boolean isActive;
``n
### SendOtpRequestDto.java
```java
public class SendOtpRequestDto {
    private String usernameOrEmail;
``n
### SessionCreateDto.java
```java
public class SessionCreateDto {
    private UUID courseId;
    private UUID batchId;
    private SessionType sessionType;
    private String title;
    private String subtitle;
    private String description;
    private String liveLink;
    private String recordedLink;
    private String resourceLink;
    private LocalDate scheduledDate;
    private String scheduledTime;
``n
### SessionReorderRequestDto.java
```java
public class SessionReorderRequestDto {
    private UUID courseId;
    private UUID batchId;
    private List<SessionOrderItem> sessionIds;
    public static class SessionOrderItem {
        private Integer index;
        private UUID id;
``n
### SessionSequenceDto.java
```java
public class SessionSequenceDto {
    private UUID id;
    private Integer sequence;
``n
### SessionUpdateDto.java
```java
public class SessionUpdateDto {
    private SessionType sessionType;
    private String title;
    private String subtitle;
    private String description;
    private String liveLink;
    private String recordedLink;
    private String resourceLink;
    private LocalDate scheduledDate;
    private String scheduledTime;
``n
### SetPasswordDto.java
```java
public class SetPasswordDto {
    private String username;
    private String otp;
    private String newPassword;
``n
### SetPasswordRequestDto.java
```java
public class SetPasswordRequestDto {
    private String otp;
    private String newPassword;
``n
### VendorAccessRequestCreateDto.java
```java
public class VendorAccessRequestCreateDto {
    private UUID courseId;
    private UUID batchId;
    private String courseName;
    private String batchName;
    private UUID partnerId;
    private String partnerName;
    private Integer maxStudents;
    private String note; // response note (optional)
``n
### VendorCreateDto.java
```java
public class VendorCreateDto {
    private String username;
    private String email;
    private String companyName;
    private String phone;
    private String description;
``n
### VendorUpdateDto.java
```java
public class VendorUpdateDto {
    private String companyName;
    private String phone;
    private String description;
    private Boolean isActive;
``n
### AccessRequestResponseDto.java
```java
public class AccessRequestResponseDto {
    private UUID id;
    private UUID partnerId;
    private UUID vendorId;
    private UUID courseId;
    private UUID batchId;
    private String partnerName;
    private String courseName;
    private String batchName;
    private String status;
    private String requestNote;
    private Boolean hasBatchAccess;
    private Integer maxStudents;
    private String responseNote;
    private String requestedAt;
``n
### ApiResponse.java
```java
public class ApiResponse<T> {
    private T data;
    private String message;
    private String error;
    private String errorCode;
    public static <T> ApiResponse<T> success(T data, String message) {
    public static <T> ApiResponse<T> success(String message) {
    public static <T> ApiResponse<T> failure(String message, String error, String errorCode) {
    public static <T> ApiResponse<T> failure(String message, String error) {
``n
### AuthResponseDto.java
```java
public class AuthResponseDto {
    private String accessToken;
    private String role; // VENDOR, PARTNER, SUPER_ADMIN
    private String username;
``n
### BatchDataDto.java
```java
public class BatchDataDto {
    private UUID id;
    private UUID courseId;
    private String courseName;
    private String title;
    private String subtitle;
    private String description;
    private String startingDate;
    private String endingDate;
    private String createdAt;
    private String updatedAt;
    private boolean hasRequestedAccess;
    private String accessStatus;
``n
### BatchProfileResponseDto.java
```java
public class BatchProfileResponseDto {
    private UUID id;
    private UUID courseId;
    private String title;
    private String subtitle;
    private String description;
    private String startingDate;
    private String endingDate;
    private String createdAt;
    private String updatedAt;
``n
### ComplaintResponseDto.java
```java
public class ComplaintResponseDto {
    private UUID id;
    private UUID studentId;
    private UUID partnerId;
    private UUID vendorId;
    private UUID courseId;
    private UUID batchId;
    private String studentName;
    private String email;
    private String phoneNumber;
    private String college;
    private String branch;
    private String academicYear;
    private String courseName;
    private String batchName;
``n
### CourseDataDto.java
```java
public class CourseDataDto {
    private UUID id;
    private String title;
    private String subtitle;
    private String description;
    private String category;
    private String createdAt;
    private String updatedAt;
    private boolean hasRequestedAccess;
    private String accessStatus;
``n
### CourseProfileResponseDto.java
```java
public class CourseProfileResponseDto {
    private UUID id;
    private String title;
    private String subtitle;
    private String description;
    private String category;
    private String createdAt;
    private String updatedAt;
``n
### DeviceDetailsDto.java
```java
public class DeviceDetailsDto {
    private String deviceIp;
    private String deviceType;
    private String device;
    private String deviceModel;
    private String osName;
    private String osVersion;
    private String clientName;
    private String clientVersion;
``n
### LoginResponseDto.java
```java
public class LoginResponseDto {
    private UserDataDto user;
    private VendorDataDto vendor;
    private PartnerDataDto partner;
``n
### PartnerAccessSessionDto.java
```java
public class PartnerAccessSessionDto {
    private UUID id;
    private UUID courseId;
    private String courseName;
    private UUID batchId;
    private String batchName;
    private String title;
    private String subtitle;
    private String description;
    private String sessionType;
    private Integer sequenceOrder;
    private String scheduledDate;
    private String scheduledTime;
    private String liveLink;
    private String recordedLink;
``n
### PartnerCalendarDayDto.java
```java
public class PartnerCalendarDayDto {
    private LocalDate date;
    private List<PartnerCalendarSessionDto> sessions;
``n
### PartnerCalendarSessionDto.java
```java
public class PartnerCalendarSessionDto {
    private UUID sessionId;
    private UUID courseId;
    private String courseName;
    private UUID batchId;
    private String batchName;
    private String title;
    private String subtitle;
    private String description;
    private Integer sequenceOrder;
    private String scheduledDate;
    private String scheduledTime;
``n
### PartnerDashboardResponseDto.java
```java
public class PartnerDashboardResponseDto {
    private VendorProfileResponseDto vendorDetails;
    private long totalStudents;
    private long activeEnrollments;
    private List<ComplaintResponseDto> recentComplaints;
``n
### PartnerDataDto.java
```java
public class PartnerDataDto {
    private String id;
    private String companyName;
    private String phone;
    private String description;
    private boolean active;
    private String vendorId;
    private String createdAt;
    private DeviceDetailsDto deviceDetails;
``n
### PartnerProfileResponseDto.java
```java
public class PartnerProfileResponseDto {
    private String id;
    private String username;
    private String email;
    private String companyName;
    private String phone;
    private String description;
    private String parentVendorId; 
    private String parentVendorCompanyName;
    private boolean isActive;
``n
### PartnerSessionResponseDto.java
```java
public class PartnerSessionResponseDto {
    private UUID id;
    private UUID courseId;
    private String courseName;
    private UUID batchId;
    private String batchName;
    private String sessionType;
    private String title;
    private String subtitle;
    private String description;
    private String liveLink;
    private String recordedLink;
    private String resourceLink;
    private Integer sequenceOrder;
    private String scheduledDate;
``n
### PartnerStudentResponseDto.java
```java
public class PartnerStudentResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String college;
    private String branch;
    private String academicYear;
    private List<EnrollmentDto> enrollments;
    public static class EnrollmentDto {
        private UUID id;
        private UUID courseId;
        private UUID batchId;
``n
### SessionDataDto.java
```java
public class SessionDataDto {
    private UUID id;
    private UUID courseId;
    private UUID batchId;
    private String sessionType;
    private String title;
    private String subtitle;
    private String description;
    private String liveLink;
    private String recordedLink;
    private String resourceLink;
    private Integer sequenceOrder;
    private String scheduledDate;
    private String scheduledTime;
    private String createdAt;
``n
### SessionProfileResponseDto.java
```java
public class SessionProfileResponseDto {
    private UUID id;
    private UUID courseId;
    private UUID batchId;
    private String sessionType;
    private String title;
    private String subtitle;
    private String description;
    private String liveLink;
    private String recordedLink;
    private String resourceLink;
    private Integer sequenceOrder;
    private String scheduledDate;
    private String scheduledTime;
    private String createdAt;
``n
### StudentDataSyncResponseDto.java
```java
public class StudentDataSyncResponseDto {
    private Student student;
    private StudentEnrollment enrollment;
    private StudentSessionLog sessionLog;
``n
### UserDataDto.java
```java
public class UserDataDto {
    private String id;
    private String username;
    private String email;
    private String role;
    private String status;
    private boolean emailVerified;
    private String lastLoginAt;
    private String createdAt;
    private String updatedAt;
    private DeviceDetailsDto deviceDetails;
``n
### VendorDashboardResponseDto.java
```java
public class VendorDashboardResponseDto {
    private long totalCourses;
    private long totalBatches;
    private long totalPartners;
    private long totalStudents;
    private long pendingAccessRequests;
    private List<ComplaintResponseDto> recentComplaints;
``n
### VendorDataDto.java
```java
public class VendorDataDto {
    private String id;
    private String companyName;
    private String phone;
    private String description;
    private boolean active;
    private String createdAt;
    private DeviceDetailsDto deviceDetails;
``n
### VendorProfileResponseDto.java
```java
public class VendorProfileResponseDto {
    private String id;
    private String username;
    private String email;
    private String companyName;
    private String phone;
    private String description;
    private String status; // ACTIVE, PENDING_SETUP, etc.
    private boolean isActive;
    private LocalDateTime joinedAt;
``n
## entity

### ApiLog.java
```java
public class ApiLog {
    private UUID id;
    private String username;
    private String role;
    private String sessionId;
    private String ipAddress;
    private String url;
    private String method;
    private String requestBody;
    private int statusCode;
    private boolean isError;
    private String responseBody;
    private String errorMessage;
    private String successMessage;
    private long executionTimeMs;
``n
### AuditLog.java
```java
public class AuditLog {
    private UUID id;
    private User user;
    private String action; // e.g. "PARTNER_ONBOARDED", "VENDOR_BLOCKED", "PASSWORD_RESET"
    private String entityName; // e.g. "Partner", "Vendor"
    private String entityId; // Saved as String so it can hold a UUID or a BigInt
    private String ipAddress;
    private String payload; // A serialized JSON snapshot of the request that triggered the event
    private LocalDateTime createdAt;
``n
### Batch.java
```java
public class Batch {
    private UUID id;
    private UUID courseId;
    private String title;
    private String subtitle;
    private String description;
    private LocalDate startingDate;
    private LocalDate endingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
``n
### Course.java
```java
public class Course {
    private UUID id;
    private String title;
    private String subtitle;
    private String description;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
``n
### Partner.java
```java
public class Partner {
    private UUID id;
    private User user;
    private Vendor vendor;
    private String email;
    private String companyName;
    private String phone;
    private String description;
    private boolean isActive = true;
    private LocalDateTime createdAt;
``n
### PartnerAccessRequest.java
```java
public class PartnerAccessRequest {
    private UUID id;
    private UUID partnerId;
    private UUID vendorId;
    private UUID courseId;
    private UUID batchId;
    private String partnerName;
    private String courseName;
    private String batchName;
    private AccessRequestStatus status;
    private String requestNote;
    private Boolean hasBatchAccess;
    private String responseNote;
    private Integer maxStudents;
    private LocalDateTime requestedAt;
``n
### Session.java
```java
public class Session {
    private UUID id;
    private UUID courseId;
    private UUID batchId;
    private SessionType sessionType;
    private String title;
    private String subtitle;
    private String description;
    private String liveLink;
    private String recordedLink;
    private String resourceLink;
    private Integer sequenceOrder;
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private LocalDateTime createdAt;
``n
### Student.java
```java
public class Student {
    private UUID id;
    private UUID partnerId; 
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String college;
    private String branch;
    private String academicYear;
    private boolean isDeletedByPartner = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private void computeFullName() {
``n
### StudentComplaint.java
```java
public class StudentComplaint {
    private UUID id;
    private UUID studentId;
    private UUID partnerId;
    private UUID vendorId;
    private UUID courseId;
    private UUID batchId;
    private String studentName;
    private String email;
    private String phoneNumber;
    private String college;
    private String branch;
    private String academicYear;
    private String courseName;
    private String batchName;
``n
### StudentEnrollment.java
```java
public class StudentEnrollment {
    private UUID id;
    private UUID studentId;
    private UUID courseId;
    private UUID batchId;
    private String courseName;
    private String batchName;
    private EnrollmentStatus status;
    private boolean isDeletedByPartner = false;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
``n
### StudentSessionLog.java
```java
public class StudentSessionLog {
    private UUID id;
    private UUID studentId;
    private UUID sessionId;
    private SessionType sessionType;
    private UUID courseId;
    private String courseName;
    private UUID batchId;
    private String batchName;
    private LocalDateTime entryTime;
    private LocalDateTime completionTime;
    private Integer timeSpentSec;
``n
### User.java
```java
public class User {
    private UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private Role role;
    private AccountStatus status;
    private boolean emailVerified = false;
    private LocalDateTime lastLoginAt;
    private User createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
``n
### UserSession.java
```java
public class UserSession {
    private UUID id;
    private User user;
    private String sessionId; // The JTI claim of the issued JWT
    private String ipAddress;
    private String browser;
    private String os;
    private String deviceType;
    private String device;
    private String deviceModel;
    private String osVersion;
    private String clientVersion;
    private LocalDateTime loginAt;
    private LocalDateTime logoutAt;
    private boolean isActive = true;
``n
### Vendor.java
```java
public class Vendor {
    private UUID id;
    private User user;
    private String email;
    private String companyName;
    private String phone;
    private String description;
    private boolean isActive = true;
    private LocalDateTime createdAt;
``n
### AccessRequestStatus.java
```java
public enum AccessRequestStatus {
``n
### AccountStatus.java
```java
public enum AccountStatus {
``n
### ComplaintStatus.java
```java
public enum ComplaintStatus {
``n
### EnrollmentStatus.java
```java
public enum EnrollmentStatus {
``n
### Role.java
```java
public enum Role {
``n
### SessionType.java
```java
public enum SessionType {
``n
## exception

### BusinessException.java
```java
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
``n
### GlobalExceptionHandler.java
```java
public class GlobalExceptionHandler {
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
    public ResponseEntity<ApiResponse<Void>> handleDisabledException(
    public ResponseEntity<ApiResponse<Void>> handleLockedException(
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
    public ResponseEntity<ApiResponse<Void>> handleAllOtherExceptions(
``n
### ResourceNotFoundException.java
```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
``n
## repository

### ApiLogRepository.java
```java
public interface ApiLogRepository extends JpaRepository<ApiLog, UUID> {
``n
### AuditLogRepository.java
```java
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
``n
### BatchRepository.java
```java
public interface BatchRepository extends JpaRepository<Batch, UUID> {
``n
### CourseRepository.java
```java
public interface CourseRepository extends JpaRepository<Course, UUID> {
``n
### PartnerAccessRequestRepository.java
```java
public interface PartnerAccessRequestRepository extends JpaRepository<PartnerAccessRequest, UUID> {
``n
### PartnerRepository.java
```java
public interface PartnerRepository extends JpaRepository<Partner, UUID> {
``n
### SessionRepository.java
```java
public interface SessionRepository extends JpaRepository<Session, UUID> {
``n
### StudentComplaintRepository.java
```java
public interface StudentComplaintRepository extends JpaRepository<StudentComplaint, UUID> {
``n
### StudentEnrollmentRepository.java
```java
public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, UUID> {
``n
### StudentRepository.java
```java
public interface StudentRepository extends JpaRepository<Student, UUID> {
``n
### StudentSessionLogRepository.java
```java
public interface StudentSessionLogRepository extends JpaRepository<StudentSessionLog, UUID> {
``n
### UserRepository.java
```java
public interface UserRepository extends JpaRepository<User, UUID> {
``n
### UserSessionRepository.java
```java
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
``n
### VendorRepository.java
```java
public interface VendorRepository extends JpaRepository<Vendor, UUID> {
``n
## service

### AdminService.java
```java
public class AdminService {
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final UserSessionRepository userSessionRepository;
    private final EmailService emailService;
    public void createVendor(String username, String email, String companyName) {
    public void changeVendorStatus(String vendorId, String statusStr) {
``n
### ApiLogService.java
```java
public class ApiLogService {
    private final ApiLogRepository apiLogRepository;
    public Page<ApiLog> getAllLogs(Pageable pageable) {
    public void deleteLog(UUID id) {
    public void deleteAllLogs() {
``n
### AuthService.java
```java
public class AuthService {
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final PartnerRepository partnerRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final RedisService redisService;
    private int otpMaxRequests;
    private int otpTimeFrameMinutes;
    public record LoginResult(String sessionId, LoginResponseDto data) {}
    public LoginResult login(LoginRequestDto request, DeviceDetailsDto deviceDetails) {
    public LoginResponseDto getSession(String sessionId) {
    public void logout(String sessionId) {
``n
### BatchService.java
```java
public class BatchService {
    private final BatchRepository batchRepository;
    private final CourseRepository courseRepository;
    private final SessionRepository sessionRepository;
    public BatchProfileResponseDto createBatch(BatchCreateDto dto) {
    public void updateBatch(UUID id, BatchUpdateDto dto) {
    public void deleteBatch(UUID id) {
    public List<BatchDataDto> getBatchesByCourseId(UUID courseId) {
    public List<BatchDataDto> getAllBatches() {
    public BatchProfileResponseDto getBatchById(UUID id) {
    private Batch getBatchEntity(UUID id) {
    private BatchDataDto mapToDataDto(Batch batch) {
    private BatchProfileResponseDto mapToProfileDto(Batch batch) {
``n
### CourseService.java
```java
public class CourseService {
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    public CourseProfileResponseDto createCourse(CourseCreateDto dto) {
    public void updateCourse(UUID id, CourseUpdateDto dto) {
    public void deleteCourse(UUID id) {
    public List<CourseDataDto> getAllCourses() {
    public CourseProfileResponseDto getCourseById(UUID id) {
    private Course getCourseEntity(UUID id) {
    private CourseDataDto mapToDataDto(Course course) {
    private CourseProfileResponseDto mapToProfileDto(Course course) {
``n
### DashboardService.java
```java
public interface DashboardService {
``n
### EmailService.java
```java
public class EmailService {
    private final JavaMailSender mailSender;
    private String fromEmail;
    private int otpTimeFrameMinutes;
    public void sendWelcomeEmail(String to, String username, String role) {
    public void sendOtpEmail(String to, String otp) {
    public void sendForgotUsernameEmail(String to, String username) {
``n
### PartnerAccessRequestService.java
```java
public class PartnerAccessRequestService {
    private final PartnerAccessRequestRepository accessRequestRepository;
    private final PartnerRepository partnerRepository;
    private final VendorRepository vendorRepository;
    public void createAccessRequestByVendor(VendorAccessRequestCreateDto dto, String vendorUsername) {
    public void createAccessRequestByPartner(PartnerAccessRequestCreateDto dto, String partnerUsername) {
    public void updateAccessStatus(UUID id, AccessStatusUpdateDto dto) {
    public void deleteAccessRequest(UUID id) {
    public List<AccessRequestResponseDto> getRequestsByVendorId(UUID vendorId) {
    public List<AccessRequestResponseDto> getRequestsByPartnerId(UUID partnerId) {
    private AccessRequestResponseDto mapToResponseDto(PartnerAccessRequest request) {
``n
### PartnerAccessService.java
```java
public class PartnerAccessService {
    private final PartnerRepository partnerRepository;
    private final PartnerAccessRequestRepository accessRequestRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final SessionRepository sessionRepository;
    private Partner validateAndGetPartner(UUID partnerId, String username) {
    public List<CourseDataDto> getCourses(UUID partnerId, String username) {
    public List<BatchDataDto> getBatches(UUID courseId, UUID partnerId, String username) {
    public List<PartnerAccessSessionDto> getSessions(UUID courseId, UUID batchId, UUID partnerId, String username) {
    public List<PartnerCalendarDayDto> getSchedule(LocalDate fromDate, LocalDate toDate, UUID partnerId, String username) {
    private List<PartnerCalendarDayDto> buildEmptyCalendar(LocalDate fromDate, LocalDate toDate) {
    private CourseDataDto mapToCourseDataDto(Course course, List<PartnerAccessRequest> requests) {
    private BatchDataDto mapToBatchDataDto(Batch batch, String courseName, List<PartnerAccessRequest> requests) {
    private PartnerAccessSessionDto mapToPartnerAccessSessionDto(Session session, String courseName, String batchName, boolean includeLinks, List<PartnerAccessRequest> requests) {
``n
### PartnerPortalService.java
```java
public class PartnerPortalService {
    private final PartnerAccessRequestRepository accessRequestRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final StudentSessionLogRepository studentSessionLogRepository;
    public List<CourseDataDto> getCoursesByPartnerId(UUID partnerId) {
    public List<BatchDataDto> getBatchesByPartnerAndCourse(UUID partnerId, UUID courseId) {
    public List<PartnerSessionResponseDto> getSessionsByBatchAndPartner(UUID partnerId, UUID batchId) {
    public List<PartnerStudentResponseDto> getStudentsByPartner(UUID partnerId) {
    public List<PartnerSessionResponseDto> getLimitCrossedSessionsByPartner(UUID partnerId) {
    private CourseDataDto mapToCourseDataDto(Course course) {
    private BatchDataDto mapToBatchDataDto(Batch batch, String courseName) {
``n
### PartnerService.java
```java
public class PartnerService {
    private final PartnerRepository partnerRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;
    private final PartnerAccessRequestRepository partnerAccessRequestRepository;
    public void createPartner(PartnerCreateDto dto, String callerUsername, boolean isSuperAdmin) {
    public List<PartnerProfileResponseDto> getAllPartners(String callerUsername, boolean isSuperAdmin) {
    public List<PartnerProfileResponseDto> getPartnersByVendorId(UUID vendorId, String callerUsername, boolean isSuperAdmin) {
    public PartnerProfileResponseDto getPartnerById(UUID id, String callerUsername, boolean isSuperAdmin) {
    public void updatePartner(UUID id, PartnerUpdateDto dto, String callerUsername, boolean isSuperAdmin) {
    public void deletePartner(UUID id, String callerUsername, boolean isSuperAdmin) {
    private PartnerProfileResponseDto mapToProfileResponseDto(Partner partner) {
``n
### RedisService.java
```java
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private int otpTimeFrameMinutes;
    private static final String SESSION_PREFIX    = "session:";
    private static final String OTP_SESSION_PREFIX = "otp_session:";
    private static final String OTP_COUNT_PREFIX   = "otp_count:";
    public void saveSessionData(String sessionId, LoginResponseDto data, int days) {
    public LoginResponseDto getSessionData(String sessionId) {
    public void deleteSession(String sessionId) {
    public void saveOtpSession(String token, OtpSessionData data) {
    public OtpSessionData getOtpSession(String token) {
    public void deleteOtpSession(String token) {
    public int getOtpCount(String username) {
    public int incrementOtpCount(String username) {
``n
### SessionService.java
```java
public class SessionService {
    private final SessionRepository sessionRepository;
    private final BatchRepository batchRepository;
    private final CourseRepository courseRepository;
    public SessionProfileResponseDto createSession(SessionCreateDto dto) {
    public void updateSession(UUID id, SessionUpdateDto dto) {
    public void deleteSession(UUID id) {
    public void reorderSessions(SessionReorderRequestDto request) {
    public List<SessionDataDto> getAllSessions(UUID courseId, UUID batchId, String type, String search, String sortOrder) {
    public SessionProfileResponseDto getSessionById(UUID id) {
    private Session getSessionEntity(UUID id) {
    private SessionDataDto mapToDataDto(Session session) {
    private SessionProfileResponseDto mapToProfileDto(Session session) {
``n
### StudentComplaintService.java
```java
public class StudentComplaintService {
    private final StudentComplaintRepository complaintRepository;
    private final PartnerRepository partnerRepository;
    private final VendorRepository vendorRepository;
    public void createComplaint(ComplaintCreateDto dto) {
    public void updateComplaint(UUID id, ComplaintUpdateDto dto, boolean isVendor, boolean isPartner) {
    public void deleteComplaint(UUID id) {
    public List<ComplaintResponseDto> getComplaintsByVendorId(UUID vendorId) {
    public List<ComplaintResponseDto> getComplaintsByPartnerId(UUID partnerId) {
    public ComplaintResponseDto getComplaintById(UUID id) {
    private ComplaintResponseDto mapToResponseDto(StudentComplaint complaint) {
``n
### StudentDataSyncService.java
```java
public class StudentDataSyncService {
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final StudentSessionLogRepository studentSessionLogRepository;
    public StudentDataSyncResponseDto syncStudentData(StudentDataSyncRequest request) {
``n
### VendorService.java
```java
public class VendorService {
    private final VendorRepository vendorRepository;
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;
    public void createVendor(VendorCreateDto dto) {
    public List<VendorProfileResponseDto> getAllVendors() {
    public VendorProfileResponseDto getVendorById(UUID id) {
    public void updateVendor(UUID id, VendorUpdateDto dto) {
    public void deleteVendor(UUID id) {
    public void createPartner(String vendorUsername, PartnerCreateDto dto) {
    public List<PartnerProfileResponseDto> getAllPartnersForVendor(String vendorUsername) {
    private VendorProfileResponseDto mapToProfileResponseDto(Vendor vendor) {
``n
### DashboardServiceImpl.java
```java
public class DashboardServiceImpl implements DashboardService {
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final PartnerRepository partnerRepository;
    private final StudentRepository studentRepository;
    private final StudentComplaintRepository studentComplaintRepository;
    private final PartnerAccessRequestRepository partnerAccessRequestRepository;
    public VendorDashboardResponseDto getVendorDashboard(UUID vendorId) {
    public PartnerDashboardResponseDto getPartnerDashboard(UUID partnerId) {
    private ComplaintResponseDto mapToComplaintDto(StudentComplaint c) {
``n
## security

### CookieService.java
```java
public class CookieService {
    public ResponseCookie createCookie(
    public ResponseCookie clearCookie(String cookieName) {
``n
### CustomUserDetailsService.java
```java
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
``n
### JwtAuthenticationFilter.java
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final RedisService redisService;
    protected void doFilterInternal(
    private String extractCookie(HttpServletRequest request, String cookieName) {
``n
### JwtService.java
```java
public class JwtService {
    private String secretKey;
    private long jwtExpiration; // e.g., 86400000 for 1 day
    public String extractUsername(String token) {
    public String extractSessionId(String token) {
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    public String generateToken(String username, String role) {
    private String buildToken(Map<String, Object> extraClaims, String subject, String jti, long expiration) {
    public boolean isTokenValid(String token, String username) {
    private boolean isTokenExpired(String token) {
    private Date extractExpiration(String token) {
    private Claims extractAllClaims(String token) {
    private SecretKey getSignInKey() {
``n
### UserPrincipal.java
```java
public class UserPrincipal implements UserDetails {
    private final User user;
    public Collection<? extends GrantedAuthority> getAuthorities() {
    public String getPassword() {
    public String getUsername() {
    public boolean isAccountNonExpired() {
    public boolean isAccountNonLocked() {
    public boolean isCredentialsNonExpired() {
    public boolean isEnabled() {
    public String getId() {
``n
## Properties

### application.properties
```properties
spring.application.name=bleep-learnhub-backend

server.servlet.context-path=/bleep-learnhub-backend/api/v1

# ── Verbose Request Logging ────────────────────────────────────────────────────
# Set to TRUE  → logs every request's headers, cookies, and body (dev/debug mode)
# Set to FALSE → only logs TraceID + status + duration (clean production mode)
app.logging.verbose=true


# --- PostgreSQL Configuration ---
# Use the service name 'db' as the hostname

# spring.datasource.url=jdbc:postgresql://db:5432/learnhub_db
# spring.datasource.username=myuser
# spring.datasource.password=mypassword
spring.datasource.url=jdbc:postgresql://localhost:5433/learnhub_db
spring.datasource.username=myuser
spring.datasource.password=mypassword

# JPA configuration (optional but recommended for development)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# --- Redis Configuration ---
# Use the service name 'redis' as the hostname

# spring.data.redis.host=redis
# spring.data.redis.port=6379
# spring.data.redis.password=yourredispassword
spring.data.redis.host=localhost
spring.data.redis.port=6380
spring.data.redis.password=yourredispassword




spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=niladrimajumder272001@gmail.com
spring.mail.password=frbcqahykuqewbin

# SMTP Authentication and Security Properties
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.transport.protocol=smtp

# Optional: Enable debugging to see detailed email logs in your console
spring.mail.properties.mail.debug=true



# spring.mail.host: smtp-relay.brevo.com
# spring.mail.port: 587
# spring.mail.username: your_brevo_registered_email
# pring.mail.password: your_brevo_smtp_key

# --- Security / JWT Configuration ---
application.security.jwt.secret-key=U2VjdXJlS2V5Rm9yQmxjZXBMZWFybkh1YkJhY2tlbmRXaXRoU3VmZmljaWVudExlbmd0aE9mQnl0ZXM=
application.security.jwt.expiration=86400000

# --- OTP Rate Limit Configuration ---
app.otp.max-requests=100
app.otp.time-frame-minutes=30

# --- Admin Seeder Configuration ---
# WARNING: Change these credentials before deploying to production!
app.admin.username=admin
app.admin.email=adminbleepdemo@yopmail.com
app.admin.password=Admin@123
``n

