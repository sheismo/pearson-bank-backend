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
    ACCOUNT_INACTIVE("04", "Account inactive");

    private final String code;
    private final String message;

}

