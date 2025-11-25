package com.zainab.PearsonBank.service;

import com.zainab.PearsonBank.dto.*;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    public JwtResponse authenticateUser(LoginRequest loginRequest);
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    public TokenValidationResponse validateResetToken(String token);
    public void resetPassword(ResetPasswordRequest resetPasswordRequest);

    public ResponseEntity<?> generateRefreshToken(String refreshToken);

    public void sendEmailOtp(String email, String name, String type);
    public boolean verifyEmailOtp(String email, String otp);

    public String setAppPassword(String customerId, String password);
    public String changeAppPassword(String customerId, String oldPassword, String newPassword);
    public boolean confirmAppPassword(String customerId, String password);

    public String setTransactionPin(String customerId, String transactionPin);
    public String changeTransactionPin(String customerId, String oldTransactionPin, String newTransactionPin);
    public boolean confirmTransactionPin(String customerId, String transactionPin);
}

