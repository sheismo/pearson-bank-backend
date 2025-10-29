package com.zainab.PearsonBank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public class CustomerResponse {
    @Schema(name = "Customer Id")
    private UUID customerId;

    @Schema(name = "First Name")
    private String firstName;

    @Schema(name = "Last Name")
    private String lastName;

    @Schema(name = "Email")
    private String email;

    @Schema(name = "Phone Number")
    private String phoneNumber;

    @Schema(name = "Alternative Phone Number")
    private String alternativePhoneNumber;

    @Schema(name = "Accounts")
    private List<AccountDetails> accounts;
}
