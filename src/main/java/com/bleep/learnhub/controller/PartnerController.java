package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.dto.response.PartnerProfileResponseDto;
import com.bleep.learnhub.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PARTNER')")
public class PartnerController {

    private final PartnerService partnerService;

    // Partner fetches their own business profile
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<PartnerProfileResponseDto>> getMyProfile(Authentication authentication) {
        String partnerUsername = authentication.getName();
        PartnerProfileResponseDto profile = partnerService.getPartnerProfile(partnerUsername);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully."));
    }

    // Example of a future endpoint: Partner fetches courses allocated by their Vendor
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<String>> getAllocatedCourses(Authentication authentication) {
        String partnerUsername = authentication.getName();
        return ResponseEntity.ok(ApiResponse.success("Course list will be returned here.", "Courses retrieved successfully."));
    }
}