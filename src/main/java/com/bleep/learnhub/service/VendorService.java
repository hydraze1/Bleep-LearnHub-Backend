package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.request.PartnerCreateDto;
import com.bleep.learnhub.dto.response.PartnerProfileResponseDto;
import com.bleep.learnhub.entity.Partner;
import com.bleep.learnhub.entity.User;
import com.bleep.learnhub.entity.Vendor;
import com.bleep.learnhub.entity.enums.AccountStatus;
import com.bleep.learnhub.entity.enums.Role;
import com.bleep.learnhub.repository.PartnerRepository;
import com.bleep.learnhub.repository.UserRepository;
import com.bleep.learnhub.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public void createPartner(String vendorUsername, PartnerCreateDto dto) {
        // 1. Verify username/email uniqueness
        if (userRepository.existsByUsername(dto.getUsername()) || userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Username or Email already exists");
        }

        // 2. Fetch the parent Vendor
        Vendor parentVendor = vendorRepository.findByUserUsername(vendorUsername)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        // 3. Create the User record for the Partner
        User partnerUser = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .role(Role.PARTNER)
                .status(AccountStatus.PENDING_SETUP) // Forces them through the OTP flow
                .createdBy(parentVendor.getUser()) // Audit trail
                .build();
        userRepository.save(partnerUser);

        // 4. Create the Partner profile linked to both User and Vendor
        Partner partner = Partner.builder()
                .user(partnerUser)
                .vendor(parentVendor)
                .companyName(dto.getCompanyName())
                .phone(dto.getPhone())
                .isActive(true)
                .build();
        partnerRepository.save(partner);

        // 5. Send onboarding email
        emailService.sendWelcomeEmail(dto.getEmail(), dto.getUsername(), "Partner");
    }

    public List<PartnerProfileResponseDto> getAllPartnersForVendor(String vendorUsername) {
        List<Partner> partners = partnerRepository.findByVendorUserUsername(vendorUsername);

        // Map Entities to DTOs
        return partners.stream().map(partner -> PartnerProfileResponseDto.builder()
                .id(partner.getId().toString())
                .username(partner.getUser().getUsername())
                .email(partner.getUser().getEmail())
                .companyName(partner.getCompanyName())
                .phone(partner.getPhone())
                .parentVendorId(partner.getVendor().getId().toString())
                .parentVendorCompanyName(partner.getVendor().getCompanyName())
                .isActive(partner.isActive())
                .build()
        ).collect(Collectors.toList());
    }
}