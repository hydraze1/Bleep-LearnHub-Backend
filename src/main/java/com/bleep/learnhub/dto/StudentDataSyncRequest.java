package com.bleep.learnhub.dto;

import com.bleep.learnhub.entity.enums.EnrollmentStatus;
import com.bleep.learnhub.entity.enums.SessionType;
import lombok.Data;

import java.util.UUID;

@Data
public class StudentDataSyncRequest {
    // Optional: if sent, skip saving student and use existing
    private UUID studentId;

    // Student data (used only when studentId is not sent)
    private UUID partnerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String college;
    private String branch;
    private String academicYear;

    // Optional: if sent, check if courseId+batchId match — if yes, skip enrollment save
    private UUID enrollmentId;

    // Enrollment data
    private UUID courseId;
    private UUID enrollmentBatchId;
    private String enrollmentCourseName;
    private String enrollmentBatchName;
    private EnrollmentStatus enrollmentStatus;

    // Session log data
    private UUID sessionId;
    private SessionType sessionType;
    private UUID logCourseId;
    private String logCourseName;
    private UUID logBatchId;
    private String logBatchName;
}
