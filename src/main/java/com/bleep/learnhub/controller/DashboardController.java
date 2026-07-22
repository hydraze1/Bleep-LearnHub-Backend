package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.request.PartnerUpdateDto;
import com.bleep.learnhub.dto.request.VendorUpdateDto;
import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.dto.response.PartnerDashboardResponseDto;
import com.bleep.learnhub.dto.response.PartnerProfileResponseDto;
import com.bleep.learnhub.dto.response.VendorDashboardResponseDto;
import com.bleep.learnhub.dto.response.VendorProfileResponseDto;
import com.bleep.learnhub.exception.BusinessException;
import com.bleep.learnhub.repository.PartnerRepository;
import com.bleep.learnhub.repository.VendorRepository;
import com.bleep.learnhub.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final VendorRepository vendorRepository;
    private final PartnerRepository partnerRepository;
    private final com.bleep.learnhub.service.VendorService vendorService;
    private final com.bleep.learnhub.service.PartnerService partnerService;

    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'VENDOR')")
    public ResponseEntity<ApiResponse<VendorDashboardResponseDto>> getVendorDashboard(
            @PathVariable UUID vendorId, Authentication authentication) {

        // Data isolation: a VENDOR can only view their own dashboard
        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SUPER_ADMIN"));

        if (!isSuperAdmin) {
            UUID callerVendorId = vendorRepository.findByUserUsername(authentication.getName())
                    .map(v -> v.getId())
                    .orElseThrow(() -> new BusinessException("Vendor profile not found"));
            if (!callerVendorId.equals(vendorId)) {
                throw new BusinessException("Access denied: You can only view your own dashboard");
            }
        }

        VendorDashboardResponseDto dashboard = dashboardService.getVendorDashboard(vendorId);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Vendor dashboard data retrieved successfully."));
    }

    @GetMapping("/partner/{partnerId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PARTNER')")
    public ResponseEntity<ApiResponse<PartnerDashboardResponseDto>> getPartnerDashboard(
            @PathVariable UUID partnerId, Authentication authentication) {

        // Data isolation: a PARTNER can only view their own dashboard
        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SUPER_ADMIN"));

        if (!isSuperAdmin) {
            UUID callerPartnerId = partnerRepository.findByUserUsername(authentication.getName())
                    .map(p -> p.getId())
                    .orElseThrow(() -> new BusinessException("Partner profile not found"));
            if (!callerPartnerId.equals(partnerId)) {
                throw new BusinessException("Access denied: You can only view your own dashboard");
            }
        }

        PartnerDashboardResponseDto dashboard = dashboardService.getPartnerDashboard(partnerId);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Partner dashboard data retrieved successfully."));
    }

    @GetMapping("/vendor/{vendorId}/profile")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<VendorProfileResponseDto>> getVendorProfile(
            @PathVariable UUID vendorId, Authentication authentication) {

        UUID callerVendorId = vendorRepository.findByUserUsername(authentication.getName())
                .map(v -> v.getId())
                .orElseThrow(() -> new BusinessException("Vendor profile not found"));
        
        if (!callerVendorId.equals(vendorId)) {
            throw new BusinessException("Access denied: You can only view your own profile");
        }

        VendorProfileResponseDto vendor = vendorService.getVendorById(vendorId);
        return ResponseEntity.ok(ApiResponse.success(vendor, "Vendor profile retrieved successfully."));
    }

    @PutMapping("/vendor/{vendorId}/profile")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> updateVendorProfile(
            @PathVariable UUID vendorId, 
            @Valid @RequestBody VendorUpdateDto dto,
            Authentication authentication) {

        UUID callerVendorId = vendorRepository.findByUserUsername(authentication.getName())
                .map(v -> v.getId())
                .orElseThrow(() -> new BusinessException("Vendor profile not found"));
        
        if (!callerVendorId.equals(vendorId)) {
            throw new BusinessException("Access denied: You can only update your own profile");
        }

        vendorService.updateVendor(vendorId, dto);
        return ResponseEntity.ok(ApiResponse.success("Vendor profile updated successfully."));
    }

    @GetMapping("/partner/{partnerId}/profile")
    @PreAuthorize("hasAuthority('PARTNER')")
    public ResponseEntity<ApiResponse<PartnerProfileResponseDto>> getPartnerProfile(
            @PathVariable UUID partnerId, Authentication authentication) {

        UUID callerPartnerId = partnerRepository.findByUserUsername(authentication.getName())
                .map(p -> p.getId())
                .orElseThrow(() -> new BusinessException("Partner profile not found"));
        
        if (!callerPartnerId.equals(partnerId)) {
            throw new BusinessException("Access denied: You can only view your own profile");
        }

        PartnerProfileResponseDto partner = partnerService.getPartnerById(partnerId, authentication.getName(), true);
        return ResponseEntity.ok(ApiResponse.success(partner, "Partner profile retrieved successfully."));
    }

    @PutMapping("/partner/{partnerId}/profile")
    @PreAuthorize("hasAuthority('PARTNER')")
    public ResponseEntity<ApiResponse<Void>> updatePartnerProfile(
            @PathVariable UUID partnerId, 
            @Valid @RequestBody PartnerUpdateDto dto,
            Authentication authentication) {

        UUID callerPartnerId = partnerRepository.findByUserUsername(authentication.getName())
                .map(p -> p.getId())
                .orElseThrow(() -> new BusinessException("Partner profile not found"));
        
        if (!callerPartnerId.equals(partnerId)) {
            throw new BusinessException("Access denied: You can only update your own profile");
        }

        partnerService.updatePartner(partnerId, dto, authentication.getName(), true);
        return ResponseEntity.ok(ApiResponse.success("Partner profile updated successfully."));
    }
}
