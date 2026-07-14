package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.dto.response.CourseDataDto;
import com.bleep.learnhub.dto.response.BatchDataDto;
import com.bleep.learnhub.dto.response.PartnerSessionResponseDto;
import com.bleep.learnhub.dto.response.PartnerStudentResponseDto;
import com.bleep.learnhub.service.PartnerPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/partner-portal")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('VENDOR')")
public class PartnerPortalController {

    private final PartnerPortalService partnerPortalService;

    @GetMapping("/partner/{partnerId}/courses")
    public ResponseEntity<ApiResponse<List<CourseDataDto>>> getCourses(
            @PathVariable UUID partnerId) {
        List<CourseDataDto> courses = partnerPortalService.getCoursesByPartnerId(partnerId);
        return ResponseEntity.ok(ApiResponse.success(courses, "Courses retrieved successfully"));
    }

    @GetMapping("/partner/{partnerId}/courses/{courseId}/batches")
    public ResponseEntity<ApiResponse<List<BatchDataDto>>> getBatches(
            @PathVariable UUID partnerId,
            @PathVariable UUID courseId) {
        List<BatchDataDto> batches = partnerPortalService.getBatchesByPartnerAndCourse(partnerId, courseId);
        return ResponseEntity.ok(ApiResponse.success(batches, "Batches retrieved successfully"));
    }

    @GetMapping("/partner/{partnerId}/batches/{batchId}/sessions")
    public ResponseEntity<ApiResponse<List<PartnerSessionResponseDto>>> getSessions(
            @PathVariable UUID partnerId,
            @PathVariable UUID batchId) {
        List<PartnerSessionResponseDto> sessions = partnerPortalService.getSessionsByBatchAndPartner(partnerId, batchId);
        return ResponseEntity.ok(ApiResponse.success(sessions, "Sessions retrieved successfully"));
    }

    @GetMapping("/partner/{partnerId}/students")
    public ResponseEntity<ApiResponse<List<PartnerStudentResponseDto>>> getStudents(
            @PathVariable UUID partnerId) {
        List<PartnerStudentResponseDto> students = partnerPortalService.getStudentsByPartner(partnerId);
        return ResponseEntity.ok(ApiResponse.success(students, "Students retrieved successfully"));
    }

    @GetMapping("/partner/{partnerId}/sessions/limit-crossed")
    public ResponseEntity<ApiResponse<List<PartnerSessionResponseDto>>> getLimitCrossedSessions(
            @PathVariable UUID partnerId) {
        List<PartnerSessionResponseDto> sessions = partnerPortalService.getLimitCrossedSessionsByPartner(partnerId);
        return ResponseEntity.ok(ApiResponse.success(sessions, "Limit crossed sessions retrieved successfully"));
    }
}
