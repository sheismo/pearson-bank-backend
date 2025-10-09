package com.zainab.PearsonBank.utils;

import com.zainab.PearsonBank.entity.Account;
import com.zainab.PearsonBank.entity.Customer;
import com.zainab.PearsonBank.repository.AccountRepository;
import com.zainab.PearsonBank.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

@Component
@Slf4j
public class AccountHelper {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public AccountHelper(CustomerRepository customerRepository, AccountRepository accountRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    public String generateAccountNumber() {
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
        return accountNumber.append(year).append(number).toString();
    }

    public String generateUniqueAccountNumber(int maxAttempts) {
        String accountNumber = generateAccountNumber();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (!accountRepository.existsByAccountNumber(accountNumber)) {
                log.info("Unique account number generated after {} attempt(s): {}", attempt, accountNumber);
                return accountNumber;
            }

            log.warn("Attempt {}: Account number {} already exists. Retrying...", attempt, accountNumber);
            accountNumber = generateAccountNumber();
        }

        log.error("Failed to generate a unique account number after {} attempts", maxAttempts);
        return null;
    }

    public String generateReference(UUID transactionId, UUID userId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")); // Get date in YYMMDD format

        String transactionStr = transactionId.toString().replace("-", "");
        String last4Transaction = transactionStr.substring(transactionStr.length() - 4); // Get Last 4 chars of transactionId

        String userStr = userId.toString().replace("-", "");
        String last4User = userStr.substring(userStr.length() - 4); // Get Last 4 chars of userId

        String randomDigits = String.format("%02d", new Random().nextInt(100)); // Get 2 random digits

        return date + last4Transaction + last4User + randomDigits;
    }

    public String getCustomerFullName(UUID customerId) {
        return customerRepository.findById(customerId)
                .map(c -> c.getFirstName() + " " + c.getOtherName() + " " + c.getLastName())
                .orElse(null);
    }

    public String getCustomerFullName(Customer c) {
        if (c != null) return c.getFirstName() + " " + c.getOtherName() + " " + c.getLastName();
        return null;
    }

    public boolean checkIfCustomerExists(String customerEmail) {
        return customerRepository.existsByEmail(customerEmail);
    }

    public boolean checkIfAccountExists(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }

    public boolean checkIfAccountIsActive(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        return account.getAccountStatus().equalsIgnoreCase("Active"); //returns true if account is active
    }

    public boolean checkIfAmountIsValid(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }




}
