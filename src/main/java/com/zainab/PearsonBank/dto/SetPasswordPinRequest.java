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
public class SetPasswordPinRequest {
    @Schema(name = "Customer Id")
    private String customerId;

    @Schema(name = "Password/Pin")
    private String passPin;

    @Schema(name = "Channel")
    private String channel;

    @Schema(name = "Ip Address")
    private String ipAddress;
}
