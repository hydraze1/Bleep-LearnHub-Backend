package com.bleep.learnhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Nullable: Some audit events (like a bad password flood) happen before an entity is authenticated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(nullable = false, length = 100)
    private String action; // e.g. "PARTNER_ONBOARDED", "VENDOR_BLOCKED", "PASSWORD_RESET"

    @Column(name = "entity_name", length = 100)
    private String entityName; // e.g. "Partner", "Vendor"

    @Column(name = "entity_id", length = 36) 
    private String entityId; // Saved as String so it can hold a UUID or a BigInt

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String payload; // A serialized JSON snapshot of the request that triggered the event

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}