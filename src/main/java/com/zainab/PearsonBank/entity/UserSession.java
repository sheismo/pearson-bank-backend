package com.zainab.PearsonBank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_sessions")
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id")
    private UUID sessionId;

    private UUID userId;
    @Column(columnDefinition = "TEXT")
    private String accessToken;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    private LocalDateTime lastActivity;

    @Builder.Default
    private boolean revoked = false;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String userAgent;
}

