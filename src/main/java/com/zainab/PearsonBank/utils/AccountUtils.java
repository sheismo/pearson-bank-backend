package com.zainab.PearsonBank.utils;

import com.zainab.PearsonBank.dto.CustomerRequest;
import com.zainab.PearsonBank.dto.EnquiryRequest;
import com.zainab.PearsonBank.dto.GetTransactionRequest;
import com.zainab.PearsonBank.dto.TransferRequest;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.stream.Stream;

@Slf4j
public class AccountUtils {
    public static Boolean validateCustomerRequest(CustomerRequest customerRequest) {
        Object[] fields = {
                customerRequest.getFirstName(), customerRequest.getLastName(), customerRequest.getOtherName(), customerRequest.getEmail(), customerRequest.getGender(),
                customerRequest.getAddress(), customerRequest.getPhoneNumber(), customerRequest.getAlternativePhoneNumber(), customerRequest.getCountry(), customerRequest.getState()
        };

        boolean hasEmpty = Stream.of(fields).anyMatch(AccountUtils::isEmpty);
        log.info("Customer Request has empty field?: {}", hasEmpty);

        return !hasEmpty;
    }

    public static boolean validateEnquiryRequest(EnquiryRequest request) {
        Object[] fields = {
                request.getAccountNumber(), request.getCustomerId()
        };

        boolean hasEmpty = Stream.of(fields).anyMatch(AccountUtils::isEmpty);
        log.info("Enquiry Request has empty field?: {}", hasEmpty);

        return !hasEmpty;
    }

    public static boolean validateGetTransactionRequest(GetTransactionRequest request) {
        Object[] fields = {
                request.getAccountId(), request.getCustomerId(), request.getChannel()
        };

        boolean hasEmpty = Stream.of(fields).anyMatch(AccountUtils::isEmpty);
        log.info("Get Transaction Request has empty field?: {}", hasEmpty);

        return !hasEmpty;
    }

    public static boolean validateTransferRequest(TransferRequest request) {
        Object[] fields = {
                request.getCrAccountNumber(), request.getDrAccountNumber(),  request.getCustomerId(), request.getChannel()
        };

        boolean hasEmpty = Stream.of(fields).anyMatch(AccountUtils::isEmpty);
        log.info("Transfer Request has empty field?: {}", hasEmpty);

        return !hasEmpty;
    }

    static boolean isEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof String str) return str.trim().isEmpty();
        if (value instanceof Integer i) return i == 0;
        if (value instanceof Double d) return d == 0;
        if (value instanceof Boolean b) return !b;

        return false; // it's not empty if cases above don't match
    }

    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }

        String toShow = accountNumber.substring(accountNumber.length() - 4);
        String toHide = "*".repeat(accountNumber.length() - toShow.length());
        return toHide + toShow;
    }

    public static boolean hasSufficientBalance(BigDecimal balance, BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
//        return balance.subtract(amount).compareTo(BigDecimal.ZERO) >= 0;
    }

}
