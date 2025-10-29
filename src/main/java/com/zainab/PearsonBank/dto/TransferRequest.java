package com.zainab.PearsonBank.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    @Schema(name = "Id of Customer Making Request")
    @NotNull(message = "Customer Id is required")
    private String customerId;

    @Schema(name = "Credit Account Number")
    @NotNull(message = "Credit account number is required")
    private String crAccountNumber;

    @Schema(name = "Debit Account Number")
    @NotNull(message = "Debit account number is required")
    private String drAccountNumber;

    @Schema(name = "Transfer Amount")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Schema(name = "Request Channel")
    private String channel;

    @JsonIgnore
    private String senderIp;
}
