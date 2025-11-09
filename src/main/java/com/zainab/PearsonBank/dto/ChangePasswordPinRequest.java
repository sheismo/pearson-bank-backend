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
public class ChangePasswordPinRequest {
    @Schema(name = "Customer Id")
    private String customerId;

    @Schema(name = "Old Password/Pin")
    private String oldPassPin;

    @Schema(name = "New Password/Pin")
    private String newPassPin;

    @Schema(name = "Channel")
    private String channel;

    @Schema(name = "Ip Address")
    private String ipAddress;

}
