package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.request.ComplaintCreateDto;
import com.bleep.learnhub.dto.request.ComplaintUpdateDto;
import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.dto.response.ComplaintResponseDto;
import com.bleep.learnhub.service.StudentComplaintService;
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
@RequestMapping("/complaints")
@RequiredArgsConstructor
public class StudentComplaintController {

    private final StudentComplaintService complaintService;

    // This is public
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createComplaint(@Valid @RequestBody ComplaintCreateDto dto) {
        complaintService.createComplaint(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint submitted successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VENDOR', 'PARTNER')")
    public ResponseEntity<ApiResponse<Void>> updateComplaint(
            @PathVariable UUID id,
            @Valid @RequestBody ComplaintUpdateDto dto,
            Authentication authentication) {
        
        boolean isVendor = hasRole(authentication, "VENDOR");
        boolean isPartner = hasRole(authentication, "PARTNER");
        
        complaintService.updateComplaint(id, dto, isVendor, isPartner);
        return ResponseEntity.ok(ApiResponse.success("Complaint updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> deleteComplaint(@PathVariable UUID id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity.ok(ApiResponse.success("Complaint deleted successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VENDOR', 'PARTNER')")
    public ResponseEntity<ApiResponse<List<ComplaintResponseDto>>> getComplaints(
            @RequestParam(required = false) UUID vendorId,
            @RequestParam(required = false) UUID partnerId) {
        
        List<ComplaintResponseDto> complaints;
        if (vendorId != null) {
            complaints = complaintService.getComplaintsByVendorId(vendorId);
        } else if (partnerId != null) {
            complaints = complaintService.getComplaintsByPartnerId(partnerId);
        } else {
            throw new IllegalArgumentException("Either vendorId or partnerId must be provided");
        }
        
        return ResponseEntity.ok(ApiResponse.success(complaints, "Complaints retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VENDOR', 'PARTNER')")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> getComplaintById(@PathVariable UUID id) {
        ComplaintResponseDto complaint = complaintService.getComplaintById(id);
        return ResponseEntity.ok(ApiResponse.success(complaint, "Complaint retrieved successfully"));
    }
    
    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
