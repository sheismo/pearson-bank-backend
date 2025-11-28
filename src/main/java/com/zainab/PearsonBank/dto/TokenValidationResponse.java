package com.zainab.PearsonBank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {
    private String code;
    private String message;
    private boolean valid;
    private String email;
}
