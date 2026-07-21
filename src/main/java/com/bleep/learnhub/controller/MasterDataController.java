package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.entity.*;
import com.bleep.learnhub.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/master")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class MasterDataController {

    private final MasterDataService masterDataService;

    // --- Batch ---
    @GetMapping("/batches")
    public ResponseEntity<ApiResponse<Page<Batch>>> getAllBatches(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllBatches(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), search), "Batches retrieved successfully"));
    }
    @DeleteMapping("/batches/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBatch(@PathVariable UUID id) { masterDataService.deleteBatch(id); return ResponseEntity.ok(ApiResponse.success("Batch deleted successfully")); }
    @DeleteMapping("/batches")
    public ResponseEntity<ApiResponse<Void>> deleteAllBatches() { masterDataService.deleteAllBatches(); return ResponseEntity.ok(ApiResponse.success("All batches deleted successfully")); }

    // --- Course ---
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<Page<Course>>> getAllCourses(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllCourses(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), search), "Courses retrieved successfully"));
    }
    @DeleteMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID id) { masterDataService.deleteCourse(id); return ResponseEntity.ok(ApiResponse.success("Course deleted successfully")); }
    @DeleteMapping("/courses")
    public ResponseEntity<ApiResponse<Void>> deleteAllCourses() { masterDataService.deleteAllCourses(); return ResponseEntity.ok(ApiResponse.success("All courses deleted successfully")); }

    // --- Partner ---
    @GetMapping("/partners")
    public ResponseEntity<ApiResponse<Page<Partner>>> getAllPartners(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllPartners(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), search), "Partners retrieved successfully"));
    }
    @DeleteMapping("/partners/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePartner(@PathVariable UUID id) { masterDataService.deletePartner(id); return ResponseEntity.ok(ApiResponse.success("Partner deleted successfully")); }
    @DeleteMapping("/partners")
    public ResponseEntity<ApiResponse<Void>> deleteAllPartners() { masterDataService.deleteAllPartners(); return ResponseEntity.ok(ApiResponse.success("All partners deleted successfully")); }

    // --- PartnerAccessRequest ---
    @GetMapping("/partner-access-requests")
    public ResponseEntity<ApiResponse<Page<PartnerAccessRequest>>> getAllPartnerAccessRequests(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllPartnerAccessRequests(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), search), "PartnerAccessRequests retrieved successfully"));
    }
    @DeleteMapping("/partner-access-requests/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePartnerAccessRequest(@PathVariable UUID id) { masterDataService.deletePartnerAccessRequest(id); return ResponseEntity.ok(ApiResponse.success("PartnerAccessRequest deleted successfully")); }
    @DeleteMapping("/partner-access-requests")
    public ResponseEntity<ApiResponse<Void>> deleteAllPartnerAccessRequests() { masterDataService.deleteAllPartnerAccessRequests(); return ResponseEntity.ok(ApiResponse.success("All partner access requests deleted successfully")); }

    // --- Session ---
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<Page<Session>>> getAllSessions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllSessions(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), search), "Sessions retrieved successfully"));
    }
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(@PathVariable UUID id) { masterDataService.deleteSession(id); return ResponseEntity.ok(ApiResponse.success("Session deleted successfully")); }
    @DeleteMapping("/sessions")
    public ResponseEntity<ApiResponse<Void>> deleteAllSessions() { masterDataService.deleteAllSessions(); return ResponseEntity.ok(ApiResponse.success("All sessions deleted successfully")); }

    // --- Student ---
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<Page<Student>>> getAllStudents(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllStudents(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), search), "Students retrieved successfully"));
    }
    @DeleteMapping("/students/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable UUID id) { masterDataService.deleteStudent(id); return ResponseEntity.ok(ApiResponse.success("Student deleted successfully")); }
    @DeleteMapping("/students")
    public ResponseEntity<ApiResponse<Void>> deleteAllStudents() { masterDataService.deleteAllStudents(); return ResponseEntity.ok(ApiResponse.success("All students deleted successfully")); }

    // --- StudentComplaint ---
    @GetMapping("/student-complaints")
    public ResponseEntity<ApiResponse<Page<StudentComplaint>>> getAllStudentComplaints(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllStudentComplaints(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), search), "StudentComplaints retrieved successfully"));
    }
    @DeleteMapping("/student-complaints/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudentComplaint(@PathVariable UUID id) { masterDataService.deleteStudentComplaint(id); return ResponseEntity.ok(ApiResponse.success("StudentComplaint deleted successfully")); }
    @DeleteMapping("/student-complaints")
    public ResponseEntity<ApiResponse<Void>> deleteAllStudentComplaints() { masterDataService.deleteAllStudentComplaints(); return ResponseEntity.ok(ApiResponse.success("All student complaints deleted successfully")); }

    // --- StudentEnrollment ---
    @GetMapping("/student-enrollments")
    public ResponseEntity<ApiResponse<Page<StudentEnrollment>>> getAllStudentEnrollments(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllStudentEnrollments(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), search), "StudentEnrollments retrieved successfully"));
    }
    @DeleteMapping("/student-enrollments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudentEnrollment(@PathVariable UUID id) { masterDataService.deleteStudentEnrollment(id); return ResponseEntity.ok(ApiResponse.success("StudentEnrollment deleted successfully")); }
    @DeleteMapping("/student-enrollments")
    public ResponseEntity<ApiResponse<Void>> deleteAllStudentEnrollments() { masterDataService.deleteAllStudentEnrollments(); return ResponseEntity.ok(ApiResponse.success("All student enrollments deleted successfully")); }

    // --- User ---
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllUsers(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), search), "Users retrieved successfully"));
    }
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) { masterDataService.deleteUser(id); return ResponseEntity.ok(ApiResponse.success("User deleted successfully")); }
    @DeleteMapping("/users")
    public ResponseEntity<ApiResponse<Void>> deleteAllUsers() { masterDataService.deleteAllUsers(); return ResponseEntity.ok(ApiResponse.success("All users deleted successfully")); }

    // --- UserSession ---
    @GetMapping("/user-sessions")
    public ResponseEntity<ApiResponse<Page<UserSession>>> getAllUserSessions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllUserSessions(PageRequest.of(page, size), search), "UserSessions retrieved successfully"));
    }
    @DeleteMapping("/user-sessions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUserSession(@PathVariable UUID id) { masterDataService.deleteUserSession(id); return ResponseEntity.ok(ApiResponse.success("UserSession deleted successfully")); }
    @DeleteMapping("/user-sessions")
    public ResponseEntity<ApiResponse<Void>> deleteAllUserSessions() { masterDataService.deleteAllUserSessions(); return ResponseEntity.ok(ApiResponse.success("All user sessions deleted successfully")); }

    // --- Vendor ---
    @GetMapping("/vendors")
    public ResponseEntity<ApiResponse<Page<Vendor>>> getAllVendors(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllVendors(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), search), "Vendors retrieved successfully"));
    }
    @DeleteMapping("/vendors/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable UUID id) { masterDataService.deleteVendor(id); return ResponseEntity.ok(ApiResponse.success("Vendor deleted successfully")); }
    @DeleteMapping("/vendors")
    public ResponseEntity<ApiResponse<Void>> deleteAllVendors() { masterDataService.deleteAllVendors(); return ResponseEntity.ok(ApiResponse.success("All vendors deleted successfully")); }

    // --- StudentSessionLog ---
    @GetMapping("/student-session-logs")
    public ResponseEntity<ApiResponse<Page<StudentSessionLog>>> getAllStudentSessionLogs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllStudentSessionLogs(PageRequest.of(page, size), search), "StudentSessionLogs retrieved successfully"));
    }
    @DeleteMapping("/student-session-logs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudentSessionLog(@PathVariable UUID id) { masterDataService.deleteStudentSessionLog(id); return ResponseEntity.ok(ApiResponse.success("StudentSessionLog deleted successfully")); }
    @DeleteMapping("/student-session-logs")
    public ResponseEntity<ApiResponse<Void>> deleteAllStudentSessionLogs() { masterDataService.deleteAllStudentSessionLogs(); return ResponseEntity.ok(ApiResponse.success("All student session logs deleted successfully")); }
}
