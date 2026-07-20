package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.dto.response.PartnerDashboardResponseDto;
import com.bleep.learnhub.dto.response.VendorDashboardResponseDto;
import com.bleep.learnhub.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'VENDOR')")
    public ResponseEntity<ApiResponse<VendorDashboardResponseDto>> getVendorDashboard(@PathVariable UUID vendorId) {
        VendorDashboardResponseDto dashboard = dashboardService.getVendorDashboard(vendorId);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Vendor dashboard data retrieved successfully."));
    }

    @GetMapping("/partner/{partnerId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PARTNER')")
    public ResponseEntity<ApiResponse<PartnerDashboardResponseDto>> getPartnerDashboard(@PathVariable UUID partnerId) {
        PartnerDashboardResponseDto dashboard = dashboardService.getPartnerDashboard(partnerId);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Partner dashboard data retrieved successfully."));
    }
}
