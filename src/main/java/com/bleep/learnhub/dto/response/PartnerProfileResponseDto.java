package com.bleep.learnhub.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerProfileResponseDto {
    private String id;
    private String username;
    private String email;
    private String companyName;
    private String phone;
    private String description;
    
    // It's helpful for the client to know who the parent vendor is
    private String parentVendorId; 
    private String parentVendorCompanyName;
    
    private boolean isActive;
}