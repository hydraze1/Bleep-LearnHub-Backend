package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.request.PartnerCreateDto;
import com.bleep.learnhub.service.VendorService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<String> createPartner(
            @RequestBody PartnerCreateDto dto, 
            Authentication authentication) {
        
        // authentication.getName() gets the logged-in Vendor's username
        String vendorUsername = authentication.getName(); 
        vendorService.createPartner(vendorUsername, dto);
        
        return ResponseEntity.ok("Partner created successfully under your vendor account.");
    }

    // Get a list of all partners belonging to this specific vendor
    @GetMapping("/partners")
    public ResponseEntity<?> getMyPartners(Authentication authentication) {
        String vendorUsername = authentication.getName();
        return ResponseEntity.ok(vendorService.getAllPartnersForVendor(vendorUsername));
    }
}