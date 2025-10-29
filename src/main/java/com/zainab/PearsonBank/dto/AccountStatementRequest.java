package com.zainab.PearsonBank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountStatementRequest {
    @Schema(name = "Account Number")
    private String accountNumber;

    @Schema(name = "Customer Id")
    private String customerId;

    @Schema(name = "Start Date")
    private String startDate;

    @Schema(name = "End Date")
    private String endDate;

    @Schema(name = "Channel")
    private String channel;

    @Schema(name = "Sender Ip")
    private String senderIp;
}
