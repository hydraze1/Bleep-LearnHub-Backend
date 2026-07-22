package com.bleep.learnhub.entity;

import com.bleep.learnhub.entity.enums.SessionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "student_session_logs",
    indexes = {
        @Index(name = "idx_ssl_student_id", columnList = "student_id"),
        @Index(name = "idx_ssl_session_id", columnList = "session_id"),
        @Index(name = "idx_ssl_course_id", columnList = "course_id"),
        @Index(name = "idx_ssl_batch_id", columnList = "batch_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSessionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "course_name", nullable = false, length = 255)
    private String courseName;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "batch_name", nullable = false, length = 255)
    private String batchName;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "completion_time")
    private LocalDateTime completionTime;

    @Column(name = "time_spent_sec")
    private Integer timeSpentSec;
}
