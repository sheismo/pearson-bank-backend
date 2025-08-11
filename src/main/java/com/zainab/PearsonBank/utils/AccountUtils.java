package com.zainab.PearsonBank.utils;

import com.zainab.PearsonBank.dto.CustomerRequest;
import com.zainab.PearsonBank.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.stream.Stream;

@Slf4j
public class AccountUtils {
    public static String ACCOUNT_EXISTS_CODE = "01";
    public static String ACCOUNT_EXISTS_MESSAGE = "Account exists";

    public static String generateAccountNumber() {
        log.info("Generating account number, starting at: {}", LocalDateTime.now());
        /**
         * current year + random 6 digits
         */
        int min = 100000;
        int max = 999999;

        int randomNumber = (int) Math.floor(Math.random() * (max - min + 1) + min);
        Year currentYear = Year.now();

        String year = String.valueOf(currentYear);
        String number = String.valueOf(randomNumber);

        StringBuilder accountNumber = new StringBuilder();

        log.info("Account number generation completed at: {}", LocalDateTime.now());
        return accountNumber.append(year).append(randomNumber).toString();
    }

    public static String generateUniqueAccountNumber(CustomerRepository customerRepository, int maxAttempts) {
        String accountNumber = generateAccountNumber();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (!customerRepository.existsByAccountNumber(accountNumber)) {
                log.info("Unique account number generated after {} attempt(s): {}", attempt, accountNumber);
                return accountNumber;
            }

            log.warn("Attempt {}: Account number {} already exists. Retrying...", attempt, accountNumber);
            accountNumber = generateAccountNumber();
        }

        log.error("Failed to generate a unique account number after {} attempts", maxAttempts);
        return null;
    }

    public static Boolean validateCustomerRequest(CustomerRequest customerRequest) {
        Object[] fields = {
                customerRequest.getFirstName(), customerRequest.getLastName(), customerRequest.getOtherName(), customerRequest.getEmail(), customerRequest.getGender(),
                customerRequest.getAddress(), customerRequest.getPhoneNumber(), customerRequest.getAlternativePhoneNumber(), customerRequest.getStateOfOrigin()
        };

        boolean hasEmpty = Stream.of(fields).anyMatch(AccountUtils::isEmpty);
        log.info("Has empty field?: {}", hasEmpty);

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
}
