package com.zainab.PearsonBank.controller;

import com.zainab.PearsonBank.dto.*;
import com.zainab.PearsonBank.entity.Transaction;
import com.zainab.PearsonBank.service.TransactionService;
import com.zainab.PearsonBank.utils.AccountHelper;
import com.zainab.PearsonBank.utils.AccountResponses;
import com.zainab.PearsonBank.utils.AccountUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@Slf4j
public class TransactionController {
    @Autowired
    TransactionService transactionService;

    @Autowired
    AccountHelper accountHelper;

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

    @PostMapping("/single-transfer")
    public ResponseEntity<AppResponse<?>> singleTransfer(@RequestBody TransferRequest transferRequest, HttpServletRequest request){
        log.info("Incoming request to transfer funds from ip {}", request.getRemoteAddr());

        // TODO validate transfer request method should be created in account helper (no empty, fields, amounts is valid, and customer exists
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

    @PostMapping("/get-transactions")
    public ResponseEntity<AppResponse<?>> getTransactions(@RequestBody GetTransactionRequest getTransactionRequest, HttpServletRequest request){
        log.info("Incoming request to get transaction for customer from ip {}", request.getRemoteAddr());

        // TODO get logged-in user details
        // check if the customer id passed is same as customer id of the logged-in customer
        // check if customer in the request owns the account passed
//        boolean isLoggedInCustomerMakingRequest; request.getCustomerId().equals(loggedInCustomerId)


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

        if (!accountHelper.checkIfAccountExistsById(getTransactionRequest.getAccountId())) {
            log.error("Account does not exist:::::");
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        if (!accountHelper.checkIfCustomerExistsById(getTransactionRequest.getCustomerId())) {
            log.error("Account does not exist:::::");
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.CUSTOMER_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.CUSTOMER_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        getTransactionRequest.setSenderIp(request.getRemoteAddr());

        String customerId = getTransactionRequest.getCustomerId();
        String transactionId = getTransactionRequest.getTransactionId();
        String accountNumber = accountHelper.fetchAccountDetails(getTransactionRequest.getAccountId()).getAccountNumber();
        String startDate =  getTransactionRequest.getStartDate();
        String endDate =  getTransactionRequest.getEndDate();

        if (transactionId != null && !transactionId.isEmpty()) { // get single transaction
            Transaction transaction = transactionService.getSingleTransaction(customerId, accountNumber, transactionId);

            if (transaction != null) {
                AppResponse<?> response = AppResponse.builder()
                        .responseCode(AccountResponses.SUCCESS.getCode())
                        .responseMessage(AccountResponses.SUCCESS.getMessage())
                        .data(transaction)
                        .build();

                return ResponseEntity.ok(response);
            }
        } else { // get list of transactions
            List<Transaction> transactions;
            if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                transactions = transactionService.getTransactionsForCustomer(customerId, accountNumber, startDate, endDate);
            } else {
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
        }

        AppResponse<?> response = AppResponse.builder()
                .responseCode(AccountResponses.FAILED.getCode())
                .responseMessage(AccountResponses.FAILED.getMessage())
                .data(null)
                .build();

        return ResponseEntity.internalServerError().body(response);
    }

}
