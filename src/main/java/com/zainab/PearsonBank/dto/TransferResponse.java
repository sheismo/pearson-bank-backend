package com.zainab.PearsonBank.dto;

import com.zainab.PearsonBank.types.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    @Schema(name = "Sender Account Number")
    private String senderAccount;

    @Schema(name = "Sender Account Name")
    private String senderName;

    @Schema(name = "Sender Account Balance")
    private String senderBalance;

    @Schema(name = "Beneficiary Account Number")
    private String beneficiaryAccount;

    @Schema(name = "Beneficiary Account Name")
    private String beneficiaryName;

    @Schema(name = "Transaction Amount")
    private String txnAmount;

    @Schema(name = "Transaction Reference")
    private String txnReference;

    @Schema(name = "Transaction Status")
    private TransactionStatus txnStatus;

    @Schema(name = "Transaction Date")
    private LocalDateTime txnDate;
}
