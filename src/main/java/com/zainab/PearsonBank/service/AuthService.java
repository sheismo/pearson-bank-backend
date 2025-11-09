package com.zainab.PearsonBank.service;

public interface AuthService {
    public void sendEmailOtp(String email, String name, String type);
    public boolean verifyEmailOtp(String email, String otp);

    public String setAppPassword(String customerId, String password);
    public String changeAppPassword(String customerId, String oldPassword, String newPassword);
    public boolean confirmAppPassword(String customerId, String password);

    public String setTransactionPin(String customerId, String transactionPin);
    public String changeTransactionPin(String customerId, String oldTransactionPin, String newTransactionPin);
    public boolean confirmTransactionPin(String customerId, String transactionPin);

    // public boolean login() {}
    // public boolean resetPassword() {} // user passes default password and new password
    // public boolean forgotPassword() {} // generate default password and set resetpAssword field to Y, save it to the db then send a mail conaining default pasword to user
}

