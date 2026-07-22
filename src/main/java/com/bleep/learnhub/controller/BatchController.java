package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.request.BatchCreateDto;
import com.bleep.learnhub.dto.request.BatchUpdateDto;
import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.dto.response.BatchDataDto;
import com.bleep.learnhub.dto.response.BatchProfileResponseDto;
import com.bleep.learnhub.service.BatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @PostMapping
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<BatchProfileResponseDto>> createBatch(@Valid @RequestBody BatchCreateDto dto) {
        BatchProfileResponseDto created = batchService.createBatch(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Batch created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> updateBatch(
            @PathVariable UUID id,
            @Valid @RequestBody BatchUpdateDto dto) {
        batchService.updateBatch(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Batch updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> deleteBatch(@PathVariable UUID id) {
        batchService.deleteBatch(id);
        return ResponseEntity.ok(ApiResponse.success("Batch deleted successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VENDOR', 'PARTNER')")
    public ResponseEntity<ApiResponse<List<BatchDataDto>>> getAllBatches(
            @RequestParam(required = false) UUID courseId) {
        List<BatchDataDto> batches;
        if (courseId != null) {
            batches = batchService.getBatchesByCourseId(courseId);
        } else {
            batches = batchService.getAllBatches();
        }
        return ResponseEntity.ok(ApiResponse.success(batches, "Batches retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VENDOR', 'PARTNER')")
    public ResponseEntity<ApiResponse<BatchProfileResponseDto>> getBatchById(@PathVariable UUID id) {
        BatchProfileResponseDto batch = batchService.getBatchById(id);
        return ResponseEntity.ok(ApiResponse.success(batch, "Batch retrieved successfully"));
    }
}
