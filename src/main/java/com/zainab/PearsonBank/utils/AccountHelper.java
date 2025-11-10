package com.zainab.PearsonBank.utils;

import com.zainab.PearsonBank.dto.AccountDetails;
import com.zainab.PearsonBank.dto.CustomerDetails;
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

    public String generateOTP() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public String generateReference(UUID transactionId, UUID userId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));

        String transactionStr = transactionId.toString().replace("-", "");
        String last4Transaction = transactionStr.substring(transactionStr.length() - 4);

        String userStr = userId.toString().replace("-", "");
        String last4User = userStr.substring(userStr.length() - 4);

        String randomDigits = String.format("%02d", new Random().nextInt(100));

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

    public AccountDetails fetchAccountDetails(String accountId) {
        Account account = accountRepository.findById(UUID.fromString(accountId))
                .orElseThrow(RuntimeException::new);

        if (account == null) return null;

        return AccountDetails.builder()
                .accountId(account.getId())
                .ownerId(account.getCustomer().getId())
                .accountNumber(account.getAccountNumber())
                .accountName(getCustomerFullName(account.getCustomer()))
                .accountBalance(account.getAccountBalance())
                .accountCurrency(account.getAccountCurrency())
                .accountStatus(account.getAccountStatus())
                .linkedEmail(account.getCustomer().getEmail())
                .linkedPhone(account.getCustomer().getPhoneNumber())
                .build();
    }

    public CustomerDetails fetchCustomerDetails(String customerId) {
        Customer customer =  customerRepository.findById(UUID.fromString(customerId))
                .orElseThrow(RuntimeException::new);

        if (customer == null) return null;

        return CustomerDetails.builder()
                .id(customer.getId())
                .fullName(getCustomerFullName(customer))
                .email(customer.getEmail())
                .gender(customer.getGender())
                .address(customer.getAddress())
                .phoneNumber(customer.getPhoneNumber())
                .state(customer.getState())
                .country(customer.getCountry())
                .noOfAccounts(customer.getNoOfAccounts())
                .totalBalance(customer.getTotalBalance())
                .build();
    }

    public boolean checkIfCustomerExistsByEmail(String customerEmail) {
        return customerRepository.existsByEmail(customerEmail);
    }

    public boolean checkIfCustomerExistsById(String customerId) {
        return customerRepository.existsById(UUID.fromString(customerId));
    }

    public boolean checkIfAccountExists(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }

    public boolean checkIfAccountExistsById(String accountId) {
        return accountRepository.existsById(UUID.fromString(accountId));
    }

    public boolean checkIfCustomerHasAnAccount(UUID customerId) {
        return accountRepository.existsByCustomerId(customerId);
    }

    public boolean checkIfAccountBelongsToCustomer(String customerId, String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) return false;

        return account.getCustomer().getId().equals(UUID.fromString(customerId));
    }

    public boolean checkIfCustomerIsVerified(String emailAddress) {
        Customer customer =  customerRepository.findByEmail(emailAddress)
                .orElseThrow(RuntimeException::new);

        if (customer == null) return false;
        return customer.isEmailVerified();
    }

    public boolean checkIfCustomerIsLocked(String emailAddress) {
        Customer customer =  customerRepository.findByEmail(emailAddress)
                .orElseThrow(RuntimeException::new);

        if (customer == null) return false;
        return customer.isUserLocked();
    }

    public boolean hasSetTransactionPin(String customerId) {
        Customer customer =  customerRepository.findById(UUID.fromString(customerId))
                .orElseThrow(RuntimeException::new);

        if (customer == null) return false;
        return !(customer.getTransactionPin() == null);
    }

    public boolean hasSetAppPassword(String customerId) {
        Customer customer = customerRepository.findById(UUID.fromString(customerId))
                .orElseThrow(RuntimeException::new);

        if (customer == null) return false;
        return !(customer.getAppPassword() == null);
    }

    public boolean checkIfAccountIsActive(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        return account.getAccountStatus().equalsIgnoreCase("Active"); //returns true if account is active
    }

    public boolean checkIfAmountIsValid(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

}
