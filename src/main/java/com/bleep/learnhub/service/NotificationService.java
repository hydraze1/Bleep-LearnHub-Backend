package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.*;
import com.bleep.learnhub.entity.*;
import com.bleep.learnhub.entity.enums.AccessRequestStatus;
import com.bleep.learnhub.entity.enums.NotificationTargetType;
import com.bleep.learnhub.exception.ResourceNotFoundException;
import com.bleep.learnhub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository recipientRepository;
    private final VendorRepository vendorRepository;
    private final PartnerRepository partnerRepository;
    private final PartnerAccessRequestRepository partnerAccessRequestRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final NotificationSender notificationSender;

    // ==========================================
    // VENDOR OPERATIONS
    // ==========================================

    @Transactional
    public VendorNotificationResponseDto createAndSendNotification(String vendorUsername, NotificationCreateDto createDto) {
        Vendor vendor = vendorRepository.findByUserUsername(vendorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found for username: " + vendorUsername));

        // 1. Create and Save Master Notification
        Notification notification = Notification.builder()
                .vendor(vendor)
                .title(createDto.getTitle())
                .message(createDto.getMessage())
                .targetType(createDto.getTargetType())
                .targetEntityId(createDto.getTargetEntityId())
                .priority(createDto.getPriority())
                .build();

        notification = notificationRepository.save(notification);

        // 2. Resolve Recipient Partners
        List<Partner> targetPartners = resolveTargetPartners(vendor, createDto);

        // 3. Save Recipient Records
        Notification finalNotification = notification;
        List<NotificationRecipient> recipients = targetPartners.stream()
                .map(partner -> NotificationRecipient.builder()
                        .notification(finalNotification)
                        .partner(partner)
                        .isRead(false)
                        .delivered(false)
                        .build())
                .collect(Collectors.toList());

        recipientRepository.saveAll(recipients);

        // 4. Trigger SSE Real-Time Push to Online Partners
        List<UUID> recipientPartnerIds = targetPartners.stream().map(Partner::getId).collect(Collectors.toList());
        notificationSender.sendToRecipients(notification, recipientPartnerIds);

        return mapToVendorResponseDto(notification, recipients.size(), 0);
    }

    @Transactional(readOnly = true)
    public Page<VendorNotificationResponseDto> getVendorNotifications(String vendorUsername, Pageable pageable) {
        Vendor vendor = vendorRepository.findByUserUsername(vendorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found for username: " + vendorUsername));

        Page<Notification> notifications = notificationRepository.findByVendorIdOrderByCreatedAtDesc(vendor.getId(), pageable);

        return notifications.map(notification -> {
            long totalRecipients = recipientRepository.countByNotificationId(notification.getId());
            long readCount = recipientRepository.countByNotificationIdAndIsReadTrue(notification.getId());
            return mapToVendorResponseDto(notification, totalRecipients, readCount);
        });
    }

    @Transactional
    public VendorNotificationResponseDto updateNotification(String vendorUsername, UUID notificationId, NotificationUpdateDto updateDto) {
        Vendor vendor = vendorRepository.findByUserUsername(vendorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found for username: " + vendorUsername));

        Notification notification = notificationRepository.findByIdAndVendorId(notificationId, vendor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId + " for vendor"));

        notification.setTitle(updateDto.getTitle());
        notification.setMessage(updateDto.getMessage());
        if (updateDto.getPriority() != null) {
            notification.setPriority(updateDto.getPriority());
        }

        notification = notificationRepository.save(notification);

        long totalRecipients = recipientRepository.countByNotificationId(notification.getId());
        long readCount = recipientRepository.countByNotificationIdAndIsReadTrue(notification.getId());

        return mapToVendorResponseDto(notification, totalRecipients, readCount);
    }

    @Transactional
    public void deleteNotification(String vendorUsername, UUID notificationId) {
        Vendor vendor = vendorRepository.findByUserUsername(vendorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found for username: " + vendorUsername));

        Notification notification = notificationRepository.findByIdAndVendorId(notificationId, vendor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId + " for vendor"));

        // Delete recipient records first, then master notification
        recipientRepository.deleteByNotificationId(notification.getId());
        notificationRepository.delete(notification);
    }

    // ==========================================
    // PARTNER OPERATIONS
    // ==========================================

    @Transactional(readOnly = true)
    public Page<PartnerNotificationResponseDto> getPartnerNotifications(String partnerUsername, Pageable pageable) {
        Partner partner = partnerRepository.findByUserUsername(partnerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found for username: " + partnerUsername));

        Page<NotificationRecipient> recipientPage = recipientRepository.findByPartnerIdOrderByCreatedAtDesc(partner.getId(), pageable);

        return recipientPage.map(this::mapToPartnerResponseDto);
    }

    @Transactional(readOnly = true)
    public long getPartnerUnreadCount(String partnerUsername) {
        Partner partner = partnerRepository.findByUserUsername(partnerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found for username: " + partnerUsername));

        return recipientRepository.countByPartnerIdAndIsReadFalse(partner.getId());
    }

    @Transactional
    public void markNotificationAsRead(String partnerUsername, UUID notificationId) {
        Partner partner = partnerRepository.findByUserUsername(partnerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found for username: " + partnerUsername));

        int updatedRows = recipientRepository.markAsRead(notificationId, partner.getId(), LocalDateTime.now());
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Notification recipient record not found for notification ID: " + notificationId);
        }
    }

    @Transactional
    public void markAllNotificationsAsRead(String partnerUsername) {
        Partner partner = partnerRepository.findByUserUsername(partnerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found for username: " + partnerUsername));

        recipientRepository.markAllAsReadForPartner(partner.getId(), LocalDateTime.now());
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private List<Partner> resolveTargetPartners(Vendor vendor, NotificationCreateDto dto) {
        if (dto.getTargetType() == NotificationTargetType.ALL_PARTNERS) {
            return partnerRepository.findByVendorId(vendor.getId());
        }

        if (dto.getTargetType() == NotificationTargetType.SPECIFIC_PARTNERS) {
            if (dto.getRecipientPartnerIds() == null || dto.getRecipientPartnerIds().isEmpty()) {
                throw new IllegalArgumentException("Recipient partner IDs list must be provided when targetType is SPECIFIC_PARTNERS");
            }
            return partnerRepository.findAllById(dto.getRecipientPartnerIds());
        }

        if (dto.getTargetType() == NotificationTargetType.COURSE) {
            if (dto.getTargetEntityId() == null) {
                throw new IllegalArgumentException("Course ID must be provided when targetType is COURSE");
            }
            List<PartnerAccessRequest> requests = partnerAccessRequestRepository.findByVendorId(vendor.getId());
            List<UUID> partnerIds = requests.stream()
                    .filter(r -> dto.getTargetEntityId().equals(r.getCourseId()) && r.getStatus() == AccessRequestStatus.APPROVED)
                    .map(PartnerAccessRequest::getPartnerId)
                    .distinct()
                    .collect(Collectors.toList());

            return partnerRepository.findAllById(partnerIds);
        }

        if (dto.getTargetType() == NotificationTargetType.BATCH) {
            if (dto.getTargetEntityId() == null) {
                throw new IllegalArgumentException("Batch ID must be provided when targetType is BATCH");
            }
            List<PartnerAccessRequest> requests = partnerAccessRequestRepository.findByVendorId(vendor.getId());
            List<UUID> partnerIds = requests.stream()
                    .filter(r -> dto.getTargetEntityId().equals(r.getBatchId()) && r.getStatus() == AccessRequestStatus.APPROVED)
                    .map(PartnerAccessRequest::getPartnerId)
                    .distinct()
                    .collect(Collectors.toList());

            return partnerRepository.findAllById(partnerIds);
        }

        return new ArrayList<>();
    }

    private VendorNotificationResponseDto mapToVendorResponseDto(Notification notification, long totalRecipients, long readCount) {
        String targetEntityName = null;
        if (notification.getTargetType() == NotificationTargetType.COURSE && notification.getTargetEntityId() != null) {
            targetEntityName = courseRepository.findById(notification.getTargetEntityId())
                    .map(Course::getTitle)
                    .orElse(null);
        } else if (notification.getTargetType() == NotificationTargetType.BATCH && notification.getTargetEntityId() != null) {
            targetEntityName = batchRepository.findById(notification.getTargetEntityId())
                    .map(Batch::getTitle)
                    .orElse(null);
        }

        return VendorNotificationResponseDto.builder()
                .id(notification.getId())
                .vendorId(notification.getVendor().getId())
                .vendorCompanyName(notification.getVendor().getCompanyName())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .targetType(notification.getTargetType())
                .targetEntityId(notification.getTargetEntityId())
                .targetEntityName(targetEntityName)
                .priority(notification.getPriority())
                .totalRecipients(totalRecipients)
                .readCount(readCount)
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    private PartnerNotificationResponseDto mapToPartnerResponseDto(NotificationRecipient recipient) {
        Notification notification = recipient.getNotification();
        return PartnerNotificationResponseDto.builder()
                .recipientRecordId(recipient.getId())
                .notificationId(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .vendorId(notification.getVendor().getId())
                .vendorName(notification.getVendor().getCompanyName())
                .priority(notification.getPriority())
                .isRead(recipient.getIsRead())
                .readAt(recipient.getReadAt())
                .delivered(recipient.getDelivered())
                .deliveredAt(recipient.getDeliveredAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
