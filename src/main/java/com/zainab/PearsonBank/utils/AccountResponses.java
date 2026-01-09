package com.zainab.PearsonBank.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountResponses {
    ACCOUNT_CREATION_SUCCESSFUL("00", "Account created successfully!"),
    ACCOUNT_CREATION_FAILED("09", "Failed to create account!"),
    ACCOUNT_EXISTS("01", "Account exists"),
    ACCOUNT_NOT_FOUND("02", "Account not found"),
    ACCOUNT_BLOCKED("03", "Account blocked"),
    ACCOUNT_INACTIVE("04", "Account is inactive"),
    FAILED("99", "Error occurred!"),
    SUCCESS("00", "Success!"),
    INVALID_REQUEST("40", "Invalid Request!"),
    ACCOUNT_CREDIT_SUCCESS("00", "Account credited successfully"),
    ACCOUNT_CREDIT_FAILED("42", "Error: unable to credit account!"),
    ACCOUNT_DEBIT_SUCCESS("00", "Account debited successfully"),
    ACCOUNT_DEBIT_FAILED("44", "Error: unable to debit account!"),
    INSUFFICIENT_FUNDS("11", "Insufficient Funds!"),
    FUNDS_TRANSFER_SUCCESSFUL("00", "Transfer processed successfully"),
    FUNDS_TRANSFER_FAILED("13", "Error: transfer processing failed"),
    CUSTOMER_NOT_FOUND("14", "User not found"),
    ACCOUNT_DELETION_SUCCESSFUL("00", "Account deleted successfully!"),
    ACCOUNT_DELETION_FAILED("19", "Failed to delete account!");


    private final String code;
    private final String message;

}

