package com.bleep.learnhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerDashboardResponseDto {
    private VendorProfileResponseDto vendorDetails;
    private long totalStudents;
    private long activeEnrollments;
    private List<ComplaintResponseDto> recentComplaints;
}
