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
public class TransactionPinRequest {
    @Schema(name = "User ID")
    private String customerId;

    @Schema(name = "Pin")
    private String pin;

    @Schema(name = "Request Channel")
    private String channel;
}
