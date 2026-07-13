package com.bleep.learnhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "batches",
    indexes = {
        @Index(name = "idx_batches_course_id", columnList = "course_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String subtitle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "starting_date", nullable = false)
    private LocalDate startingDate;

    @Column(name = "ending_date", nullable = false)
    private LocalDate endingDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
