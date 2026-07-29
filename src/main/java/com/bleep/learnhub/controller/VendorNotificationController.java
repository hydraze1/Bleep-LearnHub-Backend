package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.NotificationCreateDto;
import com.bleep.learnhub.dto.NotificationUpdateDto;
import com.bleep.learnhub.dto.VendorNotificationResponseDto;
import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vendors/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('VENDOR')")
public class VendorNotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<VendorNotificationResponseDto>> createNotification(
            Principal principal,
            @Valid @RequestBody NotificationCreateDto createDto) {
        VendorNotificationResponseDto response = notificationService.createAndSendNotification(principal.getName(), createDto);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification created and sent successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VendorNotificationResponseDto>>> getVendorNotifications(
            Principal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<VendorNotificationResponseDto> page = notificationService.getVendorNotifications(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Notifications history retrieved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorNotificationResponseDto>> updateNotification(
            Principal principal,
            @PathVariable UUID id,
            @Valid @RequestBody NotificationUpdateDto updateDto) {
        VendorNotificationResponseDto response = notificationService.updateNotification(principal.getName(), id, updateDto);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            Principal principal,
            @PathVariable UUID id) {
        notificationService.deleteNotification(principal.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully"));
    }
}
