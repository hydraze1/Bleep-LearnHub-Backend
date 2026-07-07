package com.bleep.learnhub.entity;

import com.bleep.learnhub.entity.enums.AccessRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "partner_access_requests",
    indexes = {
        @Index(name = "idx_par_partner_id", columnList = "partner_id"),
        @Index(name = "idx_par_vendor_id", columnList = "vendor_id"),
        @Index(name = "idx_par_course_id", columnList = "course_id"),
        @Index(name = "idx_par_batch_id", columnList = "batch_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerAccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "partner_name", nullable = false, length = 255)
    private String partnerName;

    @Column(name = "course_name", nullable = false, length = 255)
    private String courseName;

    @Column(name = "batch_name", length = 255)
    private String batchName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessRequestStatus status;

    @Column(name = "request_note", columnDefinition = "TEXT")
    private String requestNote;

    @Column(name = "response_note", columnDefinition = "TEXT")
    private String responseNote;

    @CreationTimestamp
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
