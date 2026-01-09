package com.zainab.PearsonBank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryRequest {
    @Schema(name = "User Account Number")
    private String accountNumber;

    @Schema(name = "User Id")
    private String customerId;
}
