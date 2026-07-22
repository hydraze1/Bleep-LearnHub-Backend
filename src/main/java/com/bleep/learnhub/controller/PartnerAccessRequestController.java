package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.request.AccessStatusUpdateDto;
import com.bleep.learnhub.dto.request.PartnerAccessRequestCreateDto;
import com.bleep.learnhub.dto.request.VendorAccessRequestCreateDto;
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
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> createAccessRequest(
            @Valid @RequestBody VendorAccessRequestCreateDto dto,
            Authentication authentication) {
        accessRequestService.createAccessRequestByVendor(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Access request created successfully by vendor"));
    }

    @PostMapping("/request")
    @PreAuthorize("hasAuthority('PARTNER')")
    public ResponseEntity<ApiResponse<Void>> requestAccess(
            @Valid @RequestBody PartnerAccessRequestCreateDto dto,
            Authentication authentication) {
        accessRequestService.createAccessRequestByPartner(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Access requested successfully by partner"));
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
    @PreAuthorize("hasAnyAuthority('VENDOR', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AccessRequestResponseDto>>> getPartnerRequests(@PathVariable UUID partnerId) {
        List<AccessRequestResponseDto> requests = accessRequestService.getRequestsByPartnerId(partnerId);
        return ResponseEntity.ok(ApiResponse.success(requests, "Requests retrieved successfully"));
    }
}
