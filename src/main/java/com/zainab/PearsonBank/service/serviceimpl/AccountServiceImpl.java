package com.zainab.PearsonBank.service.serviceimpl;

import com.zainab.PearsonBank.dto.*;
import com.zainab.PearsonBank.entity.Account;
import com.zainab.PearsonBank.entity.Transaction;
import com.zainab.PearsonBank.event.EmailEvent;
import com.zainab.PearsonBank.repository.AccountRepository;
import com.zainab.PearsonBank.repository.UserRepository;
import com.zainab.PearsonBank.service.AccountService;
import com.zainab.PearsonBank.service.EmailService;
import com.zainab.PearsonBank.service.TransactionService;
import com.zainab.PearsonBank.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TransactionService transactionService;
    private final AccountHelper accountHelper;
    private final PdfGenerator pdfGenerator;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${app.name}")
    private String appName;

    @Value("${app.supportMail}")
    private String appSupportMail;

    @Override
    public AppResponse<?> getAccount(String accountId) {
        log.info("Received request to get user account:::");

        boolean accountExists = accountHelper.checkIfAccountExistsById(accountId);
        if (!accountExists) {
            log.error("Account does not exist::::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
        }

        Account account = accountRepository.findById(UUID.fromString(accountId)).orElse(null);

        // Check if the logged in customer the one making the request and is the owner of the account
        UUID loggedInCustomerId = AccountUtils.getLoggedInCustomerId();
        assert account != null;
        UUID accountOwnerId = account.getUser().getId();

        boolean isValidRequest = loggedInCustomerId.equals(accountOwnerId);
        if (!isValidRequest) {
        log.error("Invalid Request - Customer is not authorized to make this request!!");
            return AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Failed: You are not authorized to make this request!")
                    .data(null)
                    .build();
        }

        return AppResponse.builder()
                .responseCode(AccountResponses.SUCCESS.getCode())
                .responseMessage(AccountResponses.SUCCESS.getMessage())
                .data(account)
                .build();
    }

    @Override
    public AppResponse<?> getAccounts(GetAccountsRequest request) {
        log.info("Received request to get user accounts:::");

        String customerId = request.getCustomerId();
        boolean customerExists = accountHelper.checkIfCustomerExistsById(customerId);
        if (!customerExists) {
            log.error("User does not exist::::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.CUSTOMER_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.CUSTOMER_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
        }

        // Check if the logged in customer the one making the request and is the owner of the account
        UUID loggedInCustomerId = AccountUtils.getLoggedInCustomerId();
        boolean isValidRequest = loggedInCustomerId.equals(UUID.fromString(customerId));
        if (!isValidRequest) {
            log.error("Invalid Request - Customer is not authorized to make this request!");
            return AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Failed: You are not authorized to make this request!")
                    .data(null)
                    .build();
        }

        // check if accounts are not empty
        List<Account> accounts = accountRepository.findByUserId(UUID.fromString(customerId));
        if (accounts == null || accounts.isEmpty()) {
            log.info("No accounts found for this user:::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_NOT_FOUND.getMessage() + " for this user")
                    .data(null)
                    .build();
        }

        return AppResponse.builder()
                .responseCode(AccountResponses.SUCCESS.getCode())
                .responseMessage(AccountResponses.SUCCESS.getMessage())
                .data(accounts)
                .build();
    }

    @Override
    public ResponseEntity<?> generateAccountStatement(String customerId, String accountNumber, String startDate, String endDate) {
        log.info("Received request to generate account statement for user with id {} and account {}",
                customerId, accountNumber);

        UUID loggedInCustomerId = AccountUtils.getLoggedInCustomerId();
        boolean isValidRequest = loggedInCustomerId.equals(UUID.fromString(customerId));
        if (!isValidRequest) {
            log.error("Invalid Request - Customer is not authorized to make this request!!!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed: You are not authorized to make this request!");
        }

        boolean accountExists = accountHelper.checkIfAccountExists(accountNumber);
        if (!accountExists) {
            log.error("Account does not exist:::::");
            return ResponseEntity.badRequest().body(new AppResponse<>(AccountResponses.ACCOUNT_NOT_FOUND.getCode(), AccountResponses.ACCOUNT_NOT_FOUND.getMessage(), null) );
        }

        boolean customerExists = accountHelper.checkIfCustomerExistsById(customerId);
        if (!customerExists) {
            log.error("User does not exist:::");
            return ResponseEntity.badRequest().body(new AppResponse<>(AccountResponses.CUSTOMER_NOT_FOUND.getCode(), AccountResponses.CUSTOMER_NOT_FOUND.getMessage(), null) );
        }

        CustomerDetails customer = accountHelper.fetchCustomerDetails(customerId);
        List<Transaction> transactions = transactionService.getTransactionsForCustomer(customerId, accountNumber, startDate, endDate);

        if (transactions == null || transactions.isEmpty()) {
            log.error("Transactions not found for user within this date range:::");
            return ResponseEntity.badRequest().body(new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), "Transactions not found for user within this date range", null) );
        }

        byte[] pdfBytes = pdfGenerator.generateStatement(transactions, accountNumber, customer, startDate, endDate);
        String filename = String.format("statement_%s_%s_to_%s.pdf",
                accountNumber,
                startDate.replace("/", "-"),
                endDate.replace("/", "-")
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @Transactional
    @Override
    public AppResponse<?> deleteAccount(DeleteAccountRequest deleteAccountRequest) {
        log.info("Received request to delete account for user with id {} and account with id{}",
                deleteAccountRequest.getCustomerId(), deleteAccountRequest.getAccountId());

        String accountId = deleteAccountRequest.getAccountId();
        String customerId = deleteAccountRequest.getCustomerId();

        AccountDetails accountDetails = accountHelper.fetchAccountDetails(accountId);
        CustomerDetails customerDetails = accountHelper.fetchCustomerDetails(customerId);

        if (!accountHelper.checkIfAccountExistsById(accountId) || !accountHelper.checkIfCustomerExistsById(customerId) ||
            !accountHelper.checkIfAccountBelongsToCustomer(customerId, accountDetails.getAccountNumber())) {
            return AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data(null)
                    .build();
        }

        if (accountDetails.getAccountBalance().compareTo(BigDecimal.ZERO) != 0) {
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_DELETION_FAILED.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_DELETION_FAILED.getMessage() + " :Account balance must be exactly 0.00 to delete!")
                    .build();
        }

        if (customerDetails.getNoOfAccounts() <= 1) {
            userRepository.deleteById(UUID.fromString(customerId)); // delete user is that is the only account
        }
        accountRepository.deleteById(UUID.fromString(accountId));
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));

        EmailDetails accountDeletionEmail = new EmailDetails();
        accountDeletionEmail.setSubject(EmailUtils.ACCOUNT_DELETION_ALERT_SUBJECT.getTemplate());
        accountDeletionEmail.setBody(EmailUtils.ACCOUNT_DELETION_ALERT_BODY.format(
                customerDetails.getFullName(), formattedDate,
                appSupportMail, appSupportMail, appName
        ));
        accountDeletionEmail.setRecipient(customerDetails.getEmail());
        eventPublisher.publishEvent(
                new EmailEvent(accountDeletionEmail)
        );

        AppResponse<?> response = new AppResponse<>();
        response.setResponseCode(AccountResponses.ACCOUNT_DELETION_SUCCESSFUL.getCode());
        response.setResponseMessage(AccountResponses.ACCOUNT_DELETION_SUCCESSFUL.getMessage());
        return response;
    }

}
