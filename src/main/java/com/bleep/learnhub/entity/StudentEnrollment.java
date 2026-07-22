package com.bleep.learnhub.entity;

import com.bleep.learnhub.entity.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "student_enrollments",
    indexes = {
        @Index(name = "idx_se_student_id", columnList = "student_id"),
        @Index(name = "idx_se_course_id", columnList = "course_id"),
        @Index(name = "idx_se_batch_id", columnList = "batch_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "course_name", nullable = false, length = 255)
    private String courseName;

    @Column(name = "batch_name", nullable = false, length = 255)
    private String batchName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    @Builder.Default
    @Column(name = "is_deleted_by_partner", nullable = false)
    private boolean isDeletedByPartner = false;

    @CreationTimestamp
    @Column(name = "enrolled_at", updatable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
