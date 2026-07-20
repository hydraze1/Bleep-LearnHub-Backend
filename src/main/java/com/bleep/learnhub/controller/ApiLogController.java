package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.entity.ApiLog;
import com.bleep.learnhub.service.ApiLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/logs")
@RequiredArgsConstructor
public class ApiLogController {

    private final ApiLogService apiLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ApiLog>>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        
        // Sort by newest first
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ApiLog> logs = apiLogService.getAllLogs(pageRequest);
        return ResponseEntity.ok(ApiResponse.success(logs, "API logs retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLog(@PathVariable UUID id) {
        apiLogService.deleteLog(id);
        return ResponseEntity.ok(ApiResponse.success("Log deleted successfully"));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAllLogs() {
        apiLogService.deleteAllLogs();
        return ResponseEntity.ok(ApiResponse.success("All logs deleted successfully"));
    }
}
