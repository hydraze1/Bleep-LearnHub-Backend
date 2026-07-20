package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.response.PartnerDashboardResponseDto;
import com.bleep.learnhub.dto.response.VendorDashboardResponseDto;

import java.util.UUID;

public interface DashboardService {
    VendorDashboardResponseDto getVendorDashboard(UUID vendorId);
    PartnerDashboardResponseDto getPartnerDashboard(UUID partnerId);
}
