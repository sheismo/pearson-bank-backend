package com.zainab.PearsonBank.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CreditDebitRequest {
    @Schema(name = "Account Number")
    @NotNull(message = "Account Number is required")
    private String accountNumber;

    @Schema(name = "Credit/Debit Amount")
    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Schema(name = "Customer Id")
    @NotNull(message = "Customer Id is required")
    private UUID customerId;

    @Schema(name = "Request Channel")
    @NotNull(message = "Channel is required")
    private String channel;

    @JsonIgnore
    private String senderIp;
}
