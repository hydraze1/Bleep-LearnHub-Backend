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
public class VendorDashboardResponseDto {
    private long totalCourses;
    private long totalBatches;
    private long totalPartners;
    private long totalStudents;
    private long pendingAccessRequests;
    private List<ComplaintResponseDto> recentComplaints;
}
