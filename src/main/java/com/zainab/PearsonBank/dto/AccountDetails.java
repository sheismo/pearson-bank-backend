package com.zainab.PearsonBank.dto;


import com.zainab.PearsonBank.types.CurrencyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDetails {
    @Schema(name = "Account Id")
    private UUID accountId;

    @Schema(name = "Account Owner Id")
    private UUID ownerId;

    @Schema(name = "Account Name")
    private String accountName;

    @Schema(name = "Account Number")
    private String accountNumber;

    @Schema(name = "Account Balance")
    private BigDecimal accountBalance;

    @Schema(name = "Account Currency")
    private CurrencyType  accountCurrency;

    @Schema(name = "Account Status")
    private String accountStatus;

    @Schema(name = "Linked Email")
    private String linkedEmail;

    @Schema(name = "Linked Phone")
    private String linkedPhone;
}
