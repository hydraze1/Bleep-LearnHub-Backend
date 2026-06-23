package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.request.VendorCreateDto;
import com.bleep.learnhub.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
// Ensures ONLY Super Admins can hit any endpoint in this file
@PreAuthorize("hasAuthority('SUPER_ADMIN')") 
public class AdminController {

    private final AdminService adminService;

    // Creates a new vendor in PENDING_SETUP state
    @PostMapping("/vendors")
    public ResponseEntity<String> createVendor(@RequestBody VendorCreateDto dto) {
        adminService.createVendor(dto.getUsername(), dto.getEmail(), dto.getCompanyName());
        return ResponseEntity.ok("Vendor created successfully. Welcome email sent.");
    }

    // Blocks a vendor from accessing the system (disables account)
    @PutMapping("/vendors/{vendorId}/block")
    public ResponseEntity<String> blockVendor(@PathVariable String vendorId) {
        adminService.changeVendorStatus(vendorId, "BLOCKED");
        return ResponseEntity.ok("Vendor blocked successfully.");
    }
}