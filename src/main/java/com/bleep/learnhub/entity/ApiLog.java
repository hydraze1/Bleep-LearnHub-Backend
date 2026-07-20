package com.bleep.learnhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // --- Caller Information ---
    @Column(length = 255)
    private String username;

    @Column(length = 50)
    private String role;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    // --- Request Details ---
    @Column(name = "api_url", length = 255, nullable = false)
    private String url;

    @Column(length = 10, nullable = false)
    private String method;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    // --- Response Details ---
    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "is_error", nullable = false)
    private boolean isError;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "success_message", columnDefinition = "TEXT")
    private String successMessage;

    // --- Execution Details ---
    @Column(name = "execution_time_ms")
    private long executionTimeMs;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
