package com.bleep.learnhub.controller;

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
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        String partnerUsername = authentication.getName();
        return ResponseEntity.ok(partnerService.getPartnerProfile(partnerUsername));
    }

    // Example of a future endpoint: Partner fetches courses allocated by their Vendor
    @GetMapping("/courses")
    public ResponseEntity<?> getAllocatedCourses(Authentication authentication) {
        String partnerUsername = authentication.getName();
        // return ResponseEntity.ok(partnerService.getAllocatedCourses(partnerUsername));
        return ResponseEntity.ok("Course list will be returned here.");
    }
}