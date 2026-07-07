package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.request.AccessRequestDto;
import com.bleep.learnhub.dto.request.AccessStatusUpdateDto;
import com.bleep.learnhub.dto.response.AccessRequestResponseDto;
import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.service.PartnerAccessRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/access-requests")
@RequiredArgsConstructor
public class PartnerAccessRequestController {

    private final PartnerAccessRequestService accessRequestService;

    @PostMapping
    @PreAuthorize("hasAuthority('PARTNER')")
    public ResponseEntity<ApiResponse<Void>> requestAccess(
            @Valid @RequestBody AccessRequestDto dto,
            Authentication authentication) {
        accessRequestService.createAccessRequest(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Access requested successfully"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> updateAccessStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AccessStatusUpdateDto dto) {
        accessRequestService.updateAccessStatus(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Access status updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> deleteAccessRecord(@PathVariable UUID id) {
        accessRequestService.deleteAccessRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Access record deleted"));
    }
    
    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("hasAnyAuthority('VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AccessRequestResponseDto>>> getVendorRequests(@PathVariable UUID vendorId) {
        List<AccessRequestResponseDto> requests = accessRequestService.getRequestsByVendorId(vendorId);
        return ResponseEntity.ok(ApiResponse.success(requests, "Requests retrieved successfully"));
    }
    
    @GetMapping("/partner/{partnerId}")
    @PreAuthorize("hasAnyAuthority('PARTNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AccessRequestResponseDto>>> getPartnerRequests(@PathVariable UUID partnerId) {
        List<AccessRequestResponseDto> requests = accessRequestService.getRequestsByPartnerId(partnerId);
        return ResponseEntity.ok(ApiResponse.success(requests, "Requests retrieved successfully"));
    }
}
