package com.bleep.learnhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponseDto {
    private UUID id;
    private UUID studentId;
    private UUID partnerId;
    private UUID vendorId;
    private UUID courseId;
    private UUID batchId;
    private String studentName;
    private String email;
    private String phoneNumber;
    private String college;
    private String branch;
    private String academicYear;
    private String courseName;
    private String batchName;
    private String complaintTitle;
    private String complaintText;
    private String status;
    private String vendorRemark;
    private String partnerRemark;
    private String createdAt;
    private String updatedAt;
}
