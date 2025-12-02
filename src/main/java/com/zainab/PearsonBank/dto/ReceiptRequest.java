package com.zainab.PearsonBank.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptRequest {
    private String transactionId;
    private String customerId;

    @JsonIgnore
    private String senderIp;
}
