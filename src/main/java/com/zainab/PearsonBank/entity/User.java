package com.zainab.PearsonBank.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String firstName;
    private String lastName;
    private String otherName;
    private String gender;
    private String address;
    private String country;
    private String state;

    @Column(unique = true, nullable = false)
    private String email;
    private String phoneNumber;
    private String alternativePhoneNumber;
    private int noOfAccounts;
    private BigDecimal totalBalance;

    private boolean multipleAccounts;

    @JsonIgnore
    private String transactionPin;

    @JsonIgnore
    private String appPassword;

    @JsonIgnore
    private boolean isDefaultPassword;

    @JsonIgnore
    private LocalDateTime defaultPasswordIssuedAt;

    private boolean emailVerified; // set to true after email verification
    private boolean profileEnabled; // set to true after initial password setup
    private LocalDateTime lastLoginDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String resetPasswordToken;
    private LocalDateTime resetPasswordTokenExpiry;
    private boolean resetPasswordTokenVerified;

    private String refreshToken;
    private LocalDateTime refreshTokenExpiry;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<Account> accounts = new ArrayList<>();

    public void addAccount(Account account) {
        accounts.add(account);
        account.setUser(this);
    }

    public void removeAccount(Account account) {
        accounts.remove(account);
        account.setUser(null);
    }

    public enum Role {
        CUSTOMER, ADMIN
    }
}
