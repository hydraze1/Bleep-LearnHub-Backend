package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.request.PartnerCreateDto;
import com.bleep.learnhub.dto.request.VendorCreateDto;
import com.bleep.learnhub.dto.request.VendorUpdateDto;
import com.bleep.learnhub.dto.response.PartnerProfileResponseDto;
import com.bleep.learnhub.dto.response.VendorProfileResponseDto;
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
public class VendorService {

    private final VendorRepository vendorRepository;
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;

    // ── SuperAdmin Vendor Management APIs ────────────────────────────────────────

    @Transactional
    public void createVendor(VendorCreateDto dto) {
        if (userRepository.existsByUsername(dto.getUsername()) || userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Username or Email already exists");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .role(Role.VENDOR)
                .status(AccountStatus.PENDING_SETUP)
                .build();
        userRepository.save(user);

        Vendor vendor = Vendor.builder()
                .user(user)
                .email(dto.getEmail())
                .companyName(dto.getCompanyName())
                .phone(dto.getPhone())
                .description(dto.getDescription())
                .isActive(true)
                .build();
        vendorRepository.save(vendor);

        emailService.sendWelcomeEmail(dto.getEmail(), dto.getUsername(), "Vendor");
    }

    @Transactional(readOnly = true)
    public List<VendorProfileResponseDto> getAllVendors() {
        return vendorRepository.findAll().stream()
                .map(this::mapToProfileResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendorProfileResponseDto getVendorById(UUID id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + id));
        return mapToProfileResponseDto(vendor);
    }

    @Transactional
    public void updateVendor(UUID id, VendorUpdateDto dto) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + id));

        vendor.setCompanyName(dto.getCompanyName());
        vendor.setPhone(dto.getPhone());
        vendor.setDescription(dto.getDescription());

        if (dto.getIsActive() != null) {
            boolean wasActive = vendor.isActive();
            boolean nowActive = dto.getIsActive();
            vendor.setActive(nowActive);

            User user = vendor.getUser();
            if (wasActive && !nowActive) {
                user.setStatus(AccountStatus.BLOCKED);
                userRepository.save(user);
                userSessionRepository.invalidateAllSessionsForUser(user.getId());
            } else if (!wasActive && nowActive) {
                user.setStatus(AccountStatus.ACTIVE);
                userRepository.save(user);
            }
        }

        vendorRepository.save(vendor);
    }

    @Transactional
    public void deleteVendor(UUID id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + id));
        User vendorUser = vendor.getUser();

        // 1. Delete all partners linked to this vendor
        List<Partner> partners = partnerRepository.findByVendorId(id);
        for (Partner partner : partners) {
            User partnerUser = partner.getUser();
            auditLogRepository.nullifyUserReferences(partnerUser.getId());
            userSessionRepository.deleteByUserId(partnerUser.getId());
            partnerRepository.delete(partner);
            userRepository.delete(partnerUser);
        }

        // 2. Delete the vendor and the vendor user
        auditLogRepository.nullifyUserReferences(vendorUser.getId());
        userSessionRepository.deleteByUserId(vendorUser.getId());
        vendorRepository.delete(vendor);
        userRepository.delete(vendorUser);
    }

    // ── Vendor Actions ───────────────────────────────────────────────────────────

    @Transactional
    public void createPartner(String vendorUsername, PartnerCreateDto dto) {
        if (userRepository.existsByUsername(dto.getUsername()) || userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Username or Email already exists");
        }

        Vendor parentVendor = vendorRepository.findByUserUsername(vendorUsername)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        User partnerUser = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .role(Role.PARTNER)
                .status(AccountStatus.PENDING_SETUP)
                .createdBy(parentVendor.getUser())
                .build();
        userRepository.save(partnerUser);

        Partner partner = Partner.builder()
                .user(partnerUser)
                .vendor(parentVendor)
                .email(dto.getEmail())
                .companyName(dto.getCompanyName())
                .phone(dto.getPhone())
                .isActive(true)
                .build();
        partnerRepository.save(partner);

        emailService.sendWelcomeEmail(dto.getEmail(), dto.getUsername(), "Partner");
    }

    public List<PartnerProfileResponseDto> getAllPartnersForVendor(String vendorUsername) {
        List<Partner> partners = partnerRepository.findByVendorUserUsername(vendorUsername);

        return partners.stream().map(partner -> PartnerProfileResponseDto.builder()
                .id(partner.getId().toString())
                .username(partner.getUser().getUsername())
                .email(partner.getEmail() != null ? partner.getEmail() : partner.getUser().getEmail())
                .companyName(partner.getCompanyName())
                .phone(partner.getPhone())
                .parentVendorId(partner.getVendor().getId().toString())
                .parentVendorCompanyName(partner.getVendor().getCompanyName())
                .isActive(partner.isActive())
                .build()
        ).collect(Collectors.toList());
    }

    // ── Mappers ──────────────────────────────────────────────────────────────────

    private VendorProfileResponseDto mapToProfileResponseDto(Vendor vendor) {
        return VendorProfileResponseDto.builder()
                .id(vendor.getId().toString())
                .username(vendor.getUser().getUsername())
                .email(vendor.getEmail() != null ? vendor.getEmail() : vendor.getUser().getEmail())
                .companyName(vendor.getCompanyName())
                .phone(vendor.getPhone())
                .description(vendor.getDescription())
                .status(vendor.getUser().getStatus().name())
                .isActive(vendor.isActive())
                .joinedAt(vendor.getCreatedAt())
                .build();
    }
}