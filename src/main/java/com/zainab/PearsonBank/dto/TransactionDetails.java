package com.zainab.PearsonBank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDetails {
    @Schema(name = "Transaction Id")
    private UUID transactionId;

    @Schema(name = "Sender Name")
    private String senderName;

    @Schema(name = "Sender Account Number")
    private String senderAccount;

    @Schema(name = "Beneficiary Name")
    private String beneficiaryName;

    @Schema(name = "Beneficiary Account Number")
    private String beneficiaryAccount;

    @Schema(name = "Transaction Amount")
    private String amount;

    @Schema(name = "Transaction Type")
    private String type;

    @Schema(name = "Reference No")
    private String referenceNo;

    @Schema(name = "Channel")
    private String channel;

    @Schema(name = "Narration")
    private String narration;

    @Schema(name = "Transaction Date")
    private LocalDateTime date;
}
