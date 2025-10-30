package com.zainab.PearsonBank.service;

public interface CredentialService {
    public void sendOtp(String email);
    public boolean verifyOtp(String email, String otp);
    public String setTransactionPin(String customerId, String transactionPin);
    public boolean confirmTransactionPin(String customerId, String transactionPin);
}
