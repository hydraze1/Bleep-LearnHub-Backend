package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.request.PartnerCreateDto;
import com.bleep.learnhub.dto.request.PartnerUpdateDto;
import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.dto.response.PartnerProfileResponseDto;
import com.bleep.learnhub.service.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/partners")
@RequiredArgsConstructor
// Ensures only Super Admins and Vendors can access these APIs
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'VENDOR')")
public class PartnerController {

    private final PartnerService partnerService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createPartner(
            @Valid @RequestBody PartnerCreateDto dto,
            Authentication authentication) {

        String username = authentication.getName();
        boolean isSuperAdmin = hasRole(authentication, "SUPER_ADMIN");

        partnerService.createPartner(dto, username, isSuperAdmin);
        return ResponseEntity.ok(ApiResponse.success("Partner created successfully. Onboarding email sent."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PartnerProfileResponseDto>>> getAllPartners(
            Authentication authentication) {

        String username = authentication.getName();
        boolean isSuperAdmin = hasRole(authentication, "SUPER_ADMIN");

        List<PartnerProfileResponseDto> partners = partnerService.getAllPartners(username, isSuperAdmin);
        return ResponseEntity.ok(ApiResponse.success(partners, "Partners list retrieved successfully."));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<List<PartnerProfileResponseDto>>> getPartnersByVendorId(
            @PathVariable UUID vendorId,
            Authentication authentication) {

        String username = authentication.getName();
        boolean isSuperAdmin = hasRole(authentication, "SUPER_ADMIN");

        List<PartnerProfileResponseDto> partners = partnerService.getPartnersByVendorId(vendorId, username, isSuperAdmin);
        return ResponseEntity.ok(ApiResponse.success(partners, "Partners retrieved successfully."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerProfileResponseDto>> getPartnerById(
            @PathVariable UUID id,
            Authentication authentication) {

        String username = authentication.getName();
        boolean isSuperAdmin = hasRole(authentication, "SUPER_ADMIN");

        PartnerProfileResponseDto partner = partnerService.getPartnerById(id, username, isSuperAdmin);
        return ResponseEntity.ok(ApiResponse.success(partner, "Partner profile retrieved successfully."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updatePartner(
            @PathVariable UUID id,
            @Valid @RequestBody PartnerUpdateDto dto,
            Authentication authentication) {

        String username = authentication.getName();
        boolean isSuperAdmin = hasRole(authentication, "SUPER_ADMIN");

        partnerService.updatePartner(id, dto, username, isSuperAdmin);
        return ResponseEntity.ok(ApiResponse.success("Partner profile updated successfully."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePartner(
            @PathVariable UUID id,
            Authentication authentication) {

        String username = authentication.getName();
        boolean isSuperAdmin = hasRole(authentication, "SUPER_ADMIN");

        partnerService.deletePartner(id, username, isSuperAdmin);
        return ResponseEntity.ok(ApiResponse.success("Partner deleted successfully."));
    }

    // ── Helper ───────────────────────────────────────────────────────────────────

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}