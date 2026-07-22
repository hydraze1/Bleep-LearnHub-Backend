package com.bleep.learnhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId; // The JTI claim of the issued JWT

    @Column(name = "ip_address", length = 45) // Length 45 safely supports IPv6 addresses
    private String ipAddress;

    private String browser;
    private String os;

    @Column(name = "device_type")
    private String deviceType;

    private String device;

    @Column(name = "device_model")
    private String deviceModel;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "client_version")
    private String clientVersion;

    @CreationTimestamp
    @Column(name = "login_at", updatable = false)
    private LocalDateTime loginAt;

    @Column(name = "logout_at")
    private LocalDateTime logoutAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}