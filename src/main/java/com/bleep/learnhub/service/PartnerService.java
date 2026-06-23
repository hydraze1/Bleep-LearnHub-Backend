package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.response.PartnerProfileResponseDto;
import com.bleep.learnhub.entity.Partner;
import com.bleep.learnhub.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;

    public PartnerProfileResponseDto getPartnerProfile(String username) {
        Partner partner = partnerRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Partner profile not found"));

        return PartnerProfileResponseDto.builder()
                .id(partner.getId().toString())
                .username(partner.getUser().getUsername())
                .email(partner.getUser().getEmail())
                .companyName(partner.getCompanyName())
                .phone(partner.getPhone())
                .parentVendorId(partner.getVendor().getId().toString())
                .parentVendorCompanyName(partner.getVendor().getCompanyName())
                .isActive(partner.isActive())
                .build();
    }
}