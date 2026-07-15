package com.bleep.learnhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "students",
    indexes = {
        @Index(name = "idx_students_partner_id", columnList = "partner_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "partner_id", nullable = false)
    private UUID partnerId; 

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "full_name", nullable = false, length = 205)
    private String fullName;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String college;

    @Column(length = 100)
    private String branch;

    @Column(name = "academic_year", length = 50)
    private String academicYear;

    @Builder.Default
    @Column(name = "is_deleted_by_partner", nullable = false)
    private boolean isDeletedByPartner = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void computeFullName() {
        String first = this.firstName != null ? this.firstName.trim() : "";
        String last = this.lastName != null ? this.lastName.trim() : "";
        this.fullName = (first + " " + last).trim();
    }
}
