package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.request.PartnerCreateDto;
import com.bleep.learnhub.dto.request.PartnerUpdateDto;
import com.bleep.learnhub.dto.response.PartnerProfileResponseDto;
import com.bleep.learnhub.entity.Partner;
import com.bleep.learnhub.entity.User;
import com.bleep.learnhub.entity.Vendor;
import com.bleep.learnhub.entity.enums.AccountStatus;
import com.bleep.learnhub.entity.enums.Role;
import com.bleep.learnhub.repository.AuditLogRepository;
import com.bleep.learnhub.repository.PartnerRepository;
import com.bleep.learnhub.repository.UserRepository;
import com.bleep.learnhub.repository.UserSessionRepository;
import com.bleep.learnhub.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;

    @Transactional
    public void createPartner(PartnerCreateDto dto, String callerUsername, boolean isSuperAdmin) {
        if (userRepository.existsByUsername(dto.getUsername()) || userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Username or Email already exists");
        }

        Vendor vendor;
        if (isSuperAdmin) {
            if (dto.getVendorId() == null || dto.getVendorId().isBlank()) {
                throw new RuntimeException("Vendor ID is required for SuperAdmin to create a Partner");
            }
            vendor = vendorRepository.findById(UUID.fromString(dto.getVendorId()))
                    .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + dto.getVendorId()));
        } else {
            vendor = vendorRepository.findByUserUsername(callerUsername)
                    .orElseThrow(() -> new RuntimeException("Vendor profile not found for user: " + callerUsername));
        }

        User creator = userRepository.findByUsername(callerUsername).orElse(null);

        User partnerUser = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .role(Role.PARTNER)
                .status(AccountStatus.PENDING_SETUP)
                .createdBy(creator)
                .build();
        userRepository.save(partnerUser);

        Partner partner = Partner.builder()
                .user(partnerUser)
                .vendor(vendor)
                .email(dto.getEmail())
                .companyName(dto.getCompanyName())
                .phone(dto.getPhone())
                .description(dto.getDescription())
                .isActive(true)
                .build();
        partnerRepository.save(partner);

        emailService.sendWelcomeEmail(dto.getEmail(), dto.getUsername(), "Partner");
    }

    @Transactional(readOnly = true)
    public List<PartnerProfileResponseDto> getAllPartners(String callerUsername, boolean isSuperAdmin) {
        if (isSuperAdmin) {
            return partnerRepository.findAll().stream()
                    .map(this::mapToProfileResponseDto)
                    .collect(Collectors.toList());
        } else {
            return partnerRepository.findByVendorUserUsername(callerUsername).stream()
                    .map(this::mapToProfileResponseDto)
                    .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public List<PartnerProfileResponseDto> getPartnersByVendorId(UUID vendorId, String callerUsername, boolean isSuperAdmin) {
        if (!isSuperAdmin) {
            Vendor vendor = vendorRepository.findByUserUsername(callerUsername)
                    .orElseThrow(() -> new RuntimeException("Vendor profile not found"));
            if (!vendor.getId().equals(vendorId)) {
                throw new RuntimeException("Access denied: Cannot access partners of another vendor");
            }
        }
        return partnerRepository.findByVendorId(vendorId).stream()
                .map(this::mapToProfileResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PartnerProfileResponseDto getPartnerById(UUID id, String callerUsername, boolean isSuperAdmin) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partner not found with ID: " + id));

        if (!isSuperAdmin) {
            Vendor vendor = vendorRepository.findByUserUsername(callerUsername)
                    .orElseThrow(() -> new RuntimeException("Vendor profile not found"));
            if (!partner.getVendor().getId().equals(vendor.getId())) {
                throw new RuntimeException("Access denied: This partner does not belong to your vendor account");
            }
        }
        return mapToProfileResponseDto(partner);
    }

    @Transactional
    public void updatePartner(UUID id, PartnerUpdateDto dto, String callerUsername, boolean isSuperAdmin) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partner not found with ID: " + id));

        if (!isSuperAdmin) {
            Vendor vendor = vendorRepository.findByUserUsername(callerUsername)
                    .orElseThrow(() -> new RuntimeException("Vendor profile not found"));
            if (!partner.getVendor().getId().equals(vendor.getId())) {
                throw new RuntimeException("Access denied: This partner does not belong to your vendor account");
            }
        }

        partner.setCompanyName(dto.getCompanyName());
        partner.setPhone(dto.getPhone());
        partner.setDescription(dto.getDescription());

        if (dto.getIsActive() != null) {
            boolean wasActive = partner.isActive();
            boolean nowActive = dto.getIsActive();
            partner.setActive(nowActive);

            User user = partner.getUser();
            if (wasActive && !nowActive) {
                user.setStatus(AccountStatus.BLOCKED);
                userRepository.save(user);
                userSessionRepository.invalidateAllSessionsForUser(user.getId());
            } else if (!wasActive && nowActive) {
                user.setStatus(AccountStatus.ACTIVE);
                userRepository.save(user);
            }
        }

        partnerRepository.save(partner);
    }

    @Transactional
    public void deletePartner(UUID id, String callerUsername, boolean isSuperAdmin) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partner not found with ID: " + id));

        if (!isSuperAdmin) {
            Vendor vendor = vendorRepository.findByUserUsername(callerUsername)
                    .orElseThrow(() -> new RuntimeException("Vendor profile not found"));
            if (!partner.getVendor().getId().equals(vendor.getId())) {
                throw new RuntimeException("Access denied: This partner does not belong to your vendor account");
            }
        }

        User partnerUser = partner.getUser();

        // 1. Nullify references in AuditLog
        auditLogRepository.nullifyUserReferences(partnerUser.getId());

        // 2. Delete all sessions for the partner's user
        userSessionRepository.deleteByUserId(partnerUser.getId());

        // 3. Delete partner entity
        partnerRepository.delete(partner);

        // 4. Delete partner's user entity
        userRepository.delete(partnerUser);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────────

    private PartnerProfileResponseDto mapToProfileResponseDto(Partner partner) {
        return PartnerProfileResponseDto.builder()
                .id(partner.getId().toString())
                .username(partner.getUser().getUsername())
                .email(partner.getEmail() != null ? partner.getEmail() : partner.getUser().getEmail())
                .companyName(partner.getCompanyName())
                .phone(partner.getPhone())
                .description(partner.getDescription())
                .parentVendorId(partner.getVendor().getId().toString())
                .parentVendorCompanyName(partner.getVendor().getCompanyName())
                .isActive(partner.isActive())
                .build();
    }
}