package com.zainab.PearsonBank.dto;

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
public class CustomerDetails {
    @Schema(name = "Customer Id")
    private UUID id;

    @Schema(name = "Customer Name")
    private String fullName;

    @Schema(name = "Gender")
    private String gender;

    @Schema(name = "Home Address")
    private String address;

    @Schema(name = "Country")
    private String country;

    @Schema(name = "State")
    private String state;

    @Schema(name = "Email Address")
    private String email;

    @Schema(name = "Phone Number")
    private String phoneNumber;

    @Schema(name = "Alternative Phone Number")
    private String alternativePhoneNumber;

    @Schema(name = "No of Accounts")
    private int noOfAccounts;

    @Schema(name = "Total Balance")
    private BigDecimal totalBalance;
}
