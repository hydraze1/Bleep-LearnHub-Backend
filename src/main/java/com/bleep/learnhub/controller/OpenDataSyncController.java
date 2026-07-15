package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.StudentDataSyncRequest;
import com.bleep.learnhub.dto.response.StudentDataSyncResponseDto;
import com.bleep.learnhub.service.StudentDataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/open/sync")
@RequiredArgsConstructor
public class OpenDataSyncController {

    private final StudentDataSyncService studentDataSyncService;

    @PostMapping("/student-data")
    public ResponseEntity<com.bleep.learnhub.dto.response.ApiResponse<StudentDataSyncResponseDto>> syncStudentData(@RequestBody StudentDataSyncRequest request) {
        StudentDataSyncResponseDto response = studentDataSyncService.syncStudentData(request);
        return ResponseEntity.ok(com.bleep.learnhub.dto.response.ApiResponse.success(response, "Student data synced successfully"));
    }
}
