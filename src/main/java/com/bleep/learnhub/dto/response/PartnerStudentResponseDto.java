package com.bleep.learnhub.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PartnerStudentResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String college;
    private String branch;
    private String academicYear;
    private List<EnrollmentDto> enrollments;

    @Data
    @Builder
    public static class EnrollmentDto {
        private UUID id;
        private UUID courseId;
        private UUID batchId;
        private String courseName;
        private String batchName;
        private String status;
        private String enrolledAt;
        private String completedAt;
    }
}
