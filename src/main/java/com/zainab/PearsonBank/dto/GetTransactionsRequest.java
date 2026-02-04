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
public class GetTransactionsRequest {
    @Schema(name = "User Id")
    private String customerId;

    @Schema(name = "Account Id")
    private String accountId;

    @Schema(name = "Start Date If Range is Needed")
    private String startDate;

    @Schema(name = "End Date If Range is Needed")
    private String endDate;

    @Schema(name = "Channel")
    private String channel;

    @Schema(name = "Sender Ip")
    private String senderIp;
}
