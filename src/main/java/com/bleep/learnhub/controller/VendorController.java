package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.request.VendorCreateDto;
import com.bleep.learnhub.dto.request.VendorUpdateDto;
import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.dto.response.VendorProfileResponseDto;
import com.bleep.learnhub.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
// Ensures only Super Admins can access any Vendor management API
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createVendor(@Valid @RequestBody VendorCreateDto dto) {
        vendorService.createVendor(dto);
        return ResponseEntity.ok(ApiResponse.success("Vendor created successfully. Onboarding email sent."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VendorProfileResponseDto>>> getAllVendors() {
        List<VendorProfileResponseDto> vendors = vendorService.getAllVendors();
        return ResponseEntity.ok(ApiResponse.success(vendors, "Vendors list retrieved successfully."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorProfileResponseDto>> getVendorById(@PathVariable UUID id) {
        VendorProfileResponseDto vendor = vendorService.getVendorById(id);
        return ResponseEntity.ok(ApiResponse.success(vendor, "Vendor profile retrieved successfully."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateVendor(@PathVariable UUID id, @Valid @RequestBody VendorUpdateDto dto) {
        vendorService.updateVendor(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Vendor profile updated successfully."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable UUID id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.ok(ApiResponse.success("Vendor and all associated partners deleted successfully."));
    }
}