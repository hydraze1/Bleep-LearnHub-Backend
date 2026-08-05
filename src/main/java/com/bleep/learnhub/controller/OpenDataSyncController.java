package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.StudentDataSyncRequest;
import com.bleep.learnhub.dto.request.ComplaintCreateDto;
import com.bleep.learnhub.dto.response.StudentDataSyncResponseDto;
import com.bleep.learnhub.service.PartnerPortalService;
import com.bleep.learnhub.dto.response.CourseDataDto;
import com.bleep.learnhub.dto.response.BatchDataDto;
import com.bleep.learnhub.service.StudentComplaintService;
import com.bleep.learnhub.service.StudentDataSyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/open/sync")
@RequiredArgsConstructor
public class OpenDataSyncController {

    private final StudentDataSyncService studentDataSyncService;
    private final StudentComplaintService studentComplaintService;
    private final PartnerPortalService partnerPortalService;

    @PostMapping("/student-data")
    public ResponseEntity<com.bleep.learnhub.dto.response.ApiResponse<StudentDataSyncResponseDto>> syncStudentData(@Valid @RequestBody StudentDataSyncRequest request) {
        StudentDataSyncResponseDto response = studentDataSyncService.syncStudentData(request);
        return ResponseEntity.ok(com.bleep.learnhub.dto.response.ApiResponse.success(response, "Student data synced successfully"));
    }

    @PostMapping("/complaints")
    public ResponseEntity<com.bleep.learnhub.dto.response.ApiResponse<Void>> createComplaint(@Valid @RequestBody ComplaintCreateDto dto) {
        studentComplaintService.createComplaint(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.bleep.learnhub.dto.response.ApiResponse.success("Complaint submitted successfully"));
    }

    @GetMapping("/courses")
    public ResponseEntity<com.bleep.learnhub.dto.response.ApiResponse<List<CourseDataDto>>> getCourses(@RequestParam UUID partnerId) {
        List<CourseDataDto> courses = partnerPortalService.getCoursesByPartnerId(partnerId);
        return ResponseEntity.ok(com.bleep.learnhub.dto.response.ApiResponse.success(courses, "Courses retrieved successfully"));
    }

    @GetMapping("/courses/{courseId}/batches")
    public ResponseEntity<com.bleep.learnhub.dto.response.ApiResponse<List<BatchDataDto>>> getBatchesByCourse(@PathVariable UUID courseId, @RequestParam UUID partnerId) {
        List<BatchDataDto> batches = partnerPortalService.getBatchesByPartnerAndCourse(partnerId, courseId);
        return ResponseEntity.ok(com.bleep.learnhub.dto.response.ApiResponse.success(batches, "Batches retrieved successfully"));
    }
}
