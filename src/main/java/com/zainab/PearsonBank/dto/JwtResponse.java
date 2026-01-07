package com.zainab.PearsonBank.dto;

import com.zainab.PearsonBank.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String id;
    private String email;
    private User.Role role;
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private LocalDateTime loginTime;
    private boolean isFirstTimeLogin;
}
