package com.zainab.PearsonBank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "email_otp")
public class EmailOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long id;

    private String email;
    private String otp;

    private LocalDateTime expiryTime;
    private boolean used = false;
}

