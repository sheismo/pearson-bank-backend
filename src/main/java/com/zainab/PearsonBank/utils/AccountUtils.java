package com.zainab.PearsonBank.utils;

import com.zainab.PearsonBank.dto.*;
import com.zainab.PearsonBank.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class AccountUtils {
    public static Boolean validateCustomerRequest(CustomerRequest customerRequest) {
        Object[] fields = {
                customerRequest.getFirstName(), customerRequest.getLastName(), customerRequest.getOtherName(), customerRequest.getEmail(), customerRequest.getGender(),
                customerRequest.getAddress(), customerRequest.getPhoneNumber(), customerRequest.getAlternativePhoneNumber(), customerRequest.getCountry(), customerRequest.getState()
        };

        boolean hasEmpty = Stream.of(fields).anyMatch(AccountUtils::isEmpty);
        log.info("User Request has empty field?: {}", hasEmpty);

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

    public static boolean validateGetTransactionsRequest(GetTransactionsRequest request) {
        Object[] fields = {
                request.getAccountId(), request.getCustomerId(), request.getChannel()
        };

        boolean hasEmpty = Stream.of(fields).anyMatch(AccountUtils::isEmpty);
        log.info("Get Transaction Request has empty field?: {}", hasEmpty);

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

    public static UUID getLoggedInCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated!");
        }

        System.out.println("User logged in: " + auth.getName());
        System.out.println("User Roles: " + auth.getAuthorities());

        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();

        return principal.getCustomerId();
    }

}
