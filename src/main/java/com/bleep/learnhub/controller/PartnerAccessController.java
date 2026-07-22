package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.dto.response.CourseDataDto;
import com.bleep.learnhub.dto.response.BatchDataDto;
import com.bleep.learnhub.dto.response.PartnerAccessSessionDto;
import com.bleep.learnhub.dto.response.PartnerCalendarDayDto;
import com.bleep.learnhub.service.PartnerAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/partner-access")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PARTNER')")
public class PartnerAccessController {

    private final PartnerAccessService partnerAccessService;

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<CourseDataDto>>> getCourses(
            @RequestParam(required = false) UUID partnerId,
            Authentication authentication) {
        List<CourseDataDto> courses = partnerAccessService.getCourses(partnerId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(courses, "Courses retrieved successfully"));
    }

    @GetMapping("/batches")
    public ResponseEntity<ApiResponse<List<BatchDataDto>>> getBatches(
            @RequestParam UUID courseId,
            @RequestParam(required = false) UUID partnerId,
            Authentication authentication) {
        List<BatchDataDto> batches = partnerAccessService.getBatches(courseId, partnerId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(batches, "Batches retrieved successfully"));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<PartnerAccessSessionDto>>> getSessions(
            @RequestParam UUID courseId,
            @RequestParam UUID batchId,
            @RequestParam(required = false) UUID partnerId,
            Authentication authentication) {
        List<PartnerAccessSessionDto> sessions = partnerAccessService.getSessions(courseId, batchId, partnerId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(sessions, "Sessions retrieved successfully"));
    }

    @GetMapping("/schedule")
    public ResponseEntity<ApiResponse<List<PartnerCalendarDayDto>>> getSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam UUID partnerId,
            Authentication authentication) {
        List<PartnerCalendarDayDto> schedule = partnerAccessService.getSchedule(fromDate, toDate, partnerId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(schedule, "Schedule retrieved successfully"));
    }
}
