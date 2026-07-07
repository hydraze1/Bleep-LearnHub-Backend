package com.bleep.learnhub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ComplaintCreateDto {

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotNull(message = "Partner ID is required")
    private UUID partnerId;

    @NotNull(message = "Vendor ID is required")
    private UUID vendorId;

    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @NotNull(message = "Batch ID is required")
    private UUID batchId;

    @NotBlank(message = "Student name is required")
    private String studentName;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    private String phoneNumber;

    private String college;

    private String branch;

    private String academicYear;

    @NotBlank(message = "Course name is required")
    private String courseName;

    @NotBlank(message = "Batch name is required")
    private String batchName;

    @NotBlank(message = "Complaint title is required")
    private String complaintTitle;

    @NotBlank(message = "Complaint text is required")
    private String complaintText;
}
