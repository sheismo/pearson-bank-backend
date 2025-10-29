package com.zainab.PearsonBank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteAccountRequest {
    private String customerId;
    private String accountId;
    private String ipAddress;
}
