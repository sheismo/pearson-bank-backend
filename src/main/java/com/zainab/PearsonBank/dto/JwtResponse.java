package com.zainab.PearsonBank.dto;

import com.zainab.PearsonBank.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String id;
    private String email;
    private Customer.Role role;
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
}
