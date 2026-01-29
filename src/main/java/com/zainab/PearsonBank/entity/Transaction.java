package com.zainab.PearsonBank.entity;

import com.zainab.PearsonBank.types.TransactionStatus;
import com.zainab.PearsonBank.types.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id")
    private UUID transactionId;

    private BigDecimal amount;
    private String drAccountName;
    private String drAccountNumber;
    private String crAccountName;
    private String crAccountNumber;
    private TransactionType type;
    private String referenceNo; // this is the tracking id
    private String channel;
    private String narration;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    private UUID initiator;
    private String initiatorIp;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status")
    private TransactionStatus transactionStatus;
}
