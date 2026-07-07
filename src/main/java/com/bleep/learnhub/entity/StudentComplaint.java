package com.bleep.learnhub.entity;

import com.bleep.learnhub.entity.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "student_complaints",
    indexes = {
        @Index(name = "idx_sc_student_id", columnList = "student_id"),
        @Index(name = "idx_sc_partner_id", columnList = "partner_id"),
        @Index(name = "idx_sc_vendor_id", columnList = "vendor_id"),
        @Index(name = "idx_sc_course_id", columnList = "course_id"),
        @Index(name = "idx_sc_batch_id", columnList = "batch_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentComplaint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "student_name", nullable = false, length = 255)
    private String studentName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String college;

    @Column(length = 100)
    private String branch;

    @Column(name = "academic_year", length = 50)
    private String academicYear;

    @Column(name = "course_name", nullable = false, length = 255)
    private String courseName;

    @Column(name = "batch_name", nullable = false, length = 255)
    private String batchName;

    @Column(name = "complaint_title", nullable = false, length = 255)
    private String complaintTitle;

    @Column(name = "complaint_text", nullable = false, columnDefinition = "TEXT")
    private String complaintText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintStatus status;

    @Column(name = "vendor_remark", columnDefinition = "TEXT")
    private String vendorRemark;

    @Column(name = "partner_remark", columnDefinition = "TEXT")
    private String partnerRemark;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
