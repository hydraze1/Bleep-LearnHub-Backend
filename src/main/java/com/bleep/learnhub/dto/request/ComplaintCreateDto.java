package com.bleep.learnhub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ComplaintCreateDto {

    private UUID studentId;

    private UUID partnerId;

    private UUID vendorId;

    private UUID courseId;

    private UUID batchId;

    private String studentName;

    @Email(message = "Valid email is required")
    private String email;

    private String phoneNumber;

    private String college;

    private String branch;

    private String academicYear;

    private String courseName;

    private String batchName;

    private String complaintTitle;

    private String complaintText;
}
