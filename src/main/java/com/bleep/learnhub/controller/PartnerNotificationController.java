package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.PartnerNotificationResponseDto;
import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.service.NotificationService;
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
@RequestMapping("/api/v1/partners/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PARTNER')")
public class PartnerNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PartnerNotificationResponseDto>>> getPartnerNotifications(
            Principal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PartnerNotificationResponseDto> page = notificationService.getPartnerNotifications(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Received notifications retrieved successfully"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Principal principal) {
        long unreadCount = notificationService.getPartnerUnreadCount(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(unreadCount, "Unread notification count retrieved successfully"));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            Principal principal,
            @PathVariable UUID id) {
        notificationService.markNotificationAsRead(principal.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Principal principal) {
        notificationService.markAllNotificationsAsRead(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }
}
