package com.zainab.PearsonBank.entity;

import com.zainab.PearsonBank.types.EmailStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String recipient;
    private String subject;

    @Column(length = 5000)
    private String body;

    private String failureReason;
    private int retryCount = 0;
    private int maxRetries = 3;

    @Enumerated(EnumType.STRING)
    private EmailStatus status;

    private LocalDateTime failedAt;
    private LocalDateTime lastTriedAt;
}

