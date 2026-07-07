package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.request.SessionCreateDto;
import com.bleep.learnhub.dto.request.SessionUpdateDto;
import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.dto.response.SessionDataDto;
import com.bleep.learnhub.dto.response.SessionProfileResponseDto;
import com.bleep.learnhub.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<SessionProfileResponseDto>> createSession(@Valid @RequestBody SessionCreateDto dto) {
        SessionProfileResponseDto created = sessionService.createSession(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Session created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> updateSession(
            @PathVariable UUID id,
            @Valid @RequestBody SessionUpdateDto dto) {
        sessionService.updateSession(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Session updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> deleteSession(@PathVariable UUID id) {
        sessionService.deleteSession(id);
        return ResponseEntity.ok(ApiResponse.success("Session deleted successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VENDOR', 'PARTNER')")
    public ResponseEntity<ApiResponse<List<SessionDataDto>>> getSessionsByBatchId(@RequestParam UUID batchId) {
        List<SessionDataDto> sessions = sessionService.getSessionsByBatchId(batchId);
        return ResponseEntity.ok(ApiResponse.success(sessions, "Sessions retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VENDOR', 'PARTNER')")
    public ResponseEntity<ApiResponse<SessionProfileResponseDto>> getSessionById(@PathVariable UUID id) {
        SessionProfileResponseDto session = sessionService.getSessionById(id);
        return ResponseEntity.ok(ApiResponse.success(session, "Session retrieved successfully"));
    }
}
