package com.zainab.PearsonBank.controller;

import com.zainab.PearsonBank.dto.*;
import com.zainab.PearsonBank.service.TransactionService;
import com.zainab.PearsonBank.utils.AccountHelper;
import com.zainab.PearsonBank.utils.AccountResponses;
import com.zainab.PearsonBank.utils.AccountUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transaction")
@Slf4j
@Tag(name = "Transaction Management APIs")
public class TransactionController {
    @Autowired
    TransactionService transactionService;

    @Autowired
    AccountHelper accountHelper;

    @Operation(summary = "Credit Account", description = "API endpoint to credit user account")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/credit-account")
    public ResponseEntity<AppResponse<?>> creditAccount(@RequestBody CreditDebitRequest creditRequest, HttpServletRequest request){
        log.info("Incoming request to credit account: : {} from ip {}", creditRequest, request.getRemoteAddr());

        if (creditRequest.getAccountNumber().isEmpty() || !accountHelper.checkIfAmountIsValid(creditRequest.getAmount())
                || String.valueOf(creditRequest.getCustomerId()).isEmpty() ) {
            log.error("Invalid Request::::::");
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        creditRequest.setSenderIp(request.getRemoteAddr());
        AppResponse<?> response = transactionService.creditAccount(creditRequest);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Debit Account", description = "API endpoint to get a user account")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/debit-account")
    public ResponseEntity<AppResponse<?>> debitAccount(@RequestBody CreditDebitRequest debitRequest, HttpServletRequest request){
        log.info("Incoming request to debit account: : {} from ip {}", debitRequest, request.getRemoteAddr());

        if (debitRequest.getAccountNumber().isEmpty() || !accountHelper.checkIfAmountIsValid(debitRequest.getAmount())
                || String.valueOf(debitRequest.getCustomerId()).isEmpty()){
            log.error("Invalid Request:::::::");
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        debitRequest.setSenderIp(request.getRemoteAddr());
        AppResponse<?> response = transactionService.debitAccount(debitRequest);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Single Transfer", description = "API endpoint to transfer funds")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/single-transfer")
    public ResponseEntity<AppResponse<?>> singleTransfer(@RequestBody TransferRequest transferRequest, HttpServletRequest request){
        log.info("Incoming request to transfer funds from ip {}", request.getRemoteAddr());

        if (!AccountUtils.validateTransferRequest(transferRequest) || !accountHelper.checkIfAmountIsValid(transferRequest.getAmount())
                || !accountHelper.checkIfCustomerExistsById(transferRequest.getCustomerId())) {

            log.error("Invalid Request:::::");
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        transferRequest.setSenderIp(request.getRemoteAddr());
        AppResponse<?> response = transactionService.transferMoney(transferRequest);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Customer Transactions ", description = "API endpoint to get transactions for customer")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/get-transactions")
    public ResponseEntity<AppResponse<?>> getTransactions(@RequestBody GetTransactionsRequest getTransactionsRequest, HttpServletRequest request) throws Exception {
        log.info("Incoming request to get transactions for user from ip {}", request.getRemoteAddr());

        String customerId = getTransactionsRequest.getCustomerId();
        String accountId  = getTransactionsRequest.getAccountId();

        AccountDetails accountDetails = accountHelper.fetchAccountDetails(getTransactionsRequest.getAccountId());
        log.info("Fetched account details {} ", accountDetails.getAccountName());
        String accountNumber = accountDetails.getAccountNumber();
        String startDate =  getTransactionsRequest.getStartDate();
        String endDate =  getTransactionsRequest.getEndDate();

        // Check if the logged in customer the one making the request and is the owner of the account
        UUID loggedInCustomerId = AccountUtils.getLoggedInCustomerId();
        boolean isValidRequest = loggedInCustomerId.equals(UUID.fromString(customerId)) &&
                loggedInCustomerId.equals(accountDetails.getOwnerId());
        if (!isValidRequest) {
            log.error("Invalid Request - Customer is not authorized to make this request:::");
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Failed: You are not authorized to make this request!")
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // validate request
        if (!AccountUtils.validateGetTransactionsRequest(getTransactionsRequest)) {
            log.error("Invalid Request::::::::::");
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        if (!accountHelper.checkIfAccountExistsById(accountId)) {
            log.error("Account does not exist:::::::::");
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        if (!accountHelper.checkIfCustomerExistsById(customerId)) {
            log.error("Account does not exist::::::::");
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.CUSTOMER_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.CUSTOMER_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        getTransactionsRequest.setSenderIp(request.getRemoteAddr());

        // get list of transactions
        List<TransactionDetails> transactions = null;
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            log.info("getting transactions from {} to {}::", startDate, endDate);
            transactions = transactionService.getTransactionsForCustomer(customerId, accountNumber, startDate, endDate);
        } else {
            log.info("getting transactions for user::" );
            transactions = transactionService.getTransactionsForCustomer(customerId, accountNumber);
        }

        if (transactions != null && !transactions.isEmpty()) {
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.SUCCESS.getCode())
                    .responseMessage(AccountResponses.SUCCESS.getMessage())
                    .data(transactions)
                    .build();

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok().body(new AppResponse<>(AccountResponses.FAILED.getCode(), "No Transactions Found", null));
    }

    @Operation(summary = "Get Transaction ", description = "API endpoint to get single transaction for customer")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/get-transaction")
    public ResponseEntity<AppResponse<?>> getTransaction(@RequestBody GetTransactionRequest getTransactionRequest, HttpServletRequest request) throws Exception {
        log.info("Incoming request to get transaction for user from ip {}", request.getRemoteAddr());

        String customerId = getTransactionRequest.getCustomerId();
        String accountId  = getTransactionRequest.getAccountId();
        String transactionId = getTransactionRequest.getTransactionId();

        AccountDetails accountDetails = accountHelper.fetchAccountDetails(getTransactionRequest.getAccountId());
        log.info("fetched account details {} ", accountDetails.getAccountName());
        String accountNumber = accountDetails.getAccountNumber();

        // Check if the logged in customer the one making the request and is the owner of the account
        UUID loggedInCustomerId = AccountUtils.getLoggedInCustomerId();
        boolean isValidRequest = loggedInCustomerId.equals(UUID.fromString(customerId)) &&
                loggedInCustomerId.equals(accountDetails.getOwnerId());
        if (!isValidRequest) {
            log.error("Invalid Request - Customer is not authorized to make this request!");
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Failed: You are not authorized to make this request!")
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // validate request
        if (!AccountUtils.validateGetTransactionRequest(getTransactionRequest)) {
            log.error("Invalid Request::::");
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        if (!accountHelper.checkIfAccountExistsById(accountId)) {
            log.error("Account does not exist:::::");
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        if (!accountHelper.checkIfCustomerExistsById(customerId)) {
            log.error("Account does not exist:::::");
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.CUSTOMER_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.CUSTOMER_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        getTransactionRequest.setSenderIp(request.getRemoteAddr());

        // get single transaction
        if (transactionId != null && !transactionId.isEmpty()) {
            TransactionDetails transaction = transactionService.getSingleTransaction(customerId, accountNumber, transactionId);

            if (transaction != null) {
                AppResponse<?> response = AppResponse.builder()
                        .responseCode(AccountResponses.SUCCESS.getCode())
                        .responseMessage(AccountResponses.SUCCESS.getMessage())
                        .data(transaction)
                        .build();

                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.internalServerError().body(new AppResponse<>(AccountResponses.FAILED.getCode(), AccountResponses.FAILED.getMessage(), null));
    }

    @Operation(summary = "Download Receipt", description = "API endpoint to download transaction receipt")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/download-receipt")
    public ResponseEntity<?> generateReceipt(@RequestBody ReceiptRequest receiptRequest, HttpServletRequest request) {
        log.info("Incoming request to get transaction receipt for from ip {}", request.getRemoteAddr());

        String transactionId = receiptRequest.getTransactionId();
        String customerId =  receiptRequest.getTransactionId();

        if (customerId == null || customerId.isEmpty() || transactionId == null || transactionId.isEmpty()) {
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data(null)
                    .build();
        }
        receiptRequest.setSenderIp(request.getRemoteAddr());

        try {
            return transactionService.generateReceiptPdf(transactionId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

}
