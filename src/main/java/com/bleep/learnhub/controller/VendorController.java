package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.request.PartnerCreateDto;
import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.dto.response.PartnerProfileResponseDto;
import com.bleep.learnhub.service.VendorService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('VENDOR')")
public class VendorController {

    private final VendorService vendorService;

    // Vendor creates a partner under their own account
    @PostMapping("/partners")
    public ResponseEntity<ApiResponse<Void>> createPartner(
            @RequestBody PartnerCreateDto dto, 
            Authentication authentication) {
        
        // authentication.getName() gets the logged-in Vendor's username
        String vendorUsername = authentication.getName(); 
        vendorService.createPartner(vendorUsername, dto);
        
        return ResponseEntity.ok(ApiResponse.success("Partner created successfully under your vendor account."));
    }

    // Get a list of all partners belonging to this specific vendor
    @GetMapping("/partners")
    public ResponseEntity<ApiResponse<List<PartnerProfileResponseDto>>> getMyPartners(Authentication authentication) {
        String vendorUsername = authentication.getName();
        List<PartnerProfileResponseDto> partners = vendorService.getAllPartnersForVendor(vendorUsername);
        return ResponseEntity.ok(ApiResponse.success(partners, "Partners list retrieved successfully."));
    }
}