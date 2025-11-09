package com.zainab.PearsonBank.service;

public interface CredentialService {
    public void sendEmailOtp(String email, String name, String type);
    public boolean verifyEmailOtp(String email, String otp);
    public String setTransactionPin(String customerId, String transactionPin);
    public boolean confirmTransactionPin(String customerId, String transactionPin);
}
