package com.zainab.PearsonBank.controller;

import com.zainab.PearsonBank.dto.AccountStatementRequest;
import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.dto.DeleteAccountRequest;
import com.zainab.PearsonBank.dto.GetAccountsRequest;
import com.zainab.PearsonBank.service.AccountService;
import com.zainab.PearsonBank.utils.AccountResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@Slf4j
@Tag(name = "Account Management APIs")
public class AccountController {
    @Autowired
    AccountService accountService;

    @Operation(summary = "Get Customer Account", description = "API endpoint to get a customer account")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/get-account")
    public ResponseEntity<AppResponse<?>> getCustomerAccount(@RequestBody String accountId, HttpServletRequest request) {
        log.info("Incoming request to get customer account from ip {}", request.getRemoteAddr());

        if (accountId == null || accountId.isEmpty()) {
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data(null)
                    .build();
        }

        AppResponse<?> response = accountService.getAccount(accountId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Customer Accounts", description = "API endpoint to get a list of all customer accounts")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/get-accounts")
    public ResponseEntity<AppResponse<?>> getCustomerAccounts(@RequestBody GetAccountsRequest accountsRequest, HttpServletRequest request) {
        log.info("Incoming request to get customer accounts name: {} from ip {}", accountsRequest, request.getRemoteAddr());

        if (accountsRequest.getCustomerId() == null || accountsRequest.getCustomerId().isEmpty()) {
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data(null)
                    .build();
        }

        AppResponse<?> response = accountService.getAccounts(accountsRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Customer Account Statement", description = "API endpoint to get account statement")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/get-account-statement")
    public ResponseEntity<AppResponse<?>> getAccountStatement(@RequestBody AccountStatementRequest accountStatementRequest, HttpServletRequest request) {
        log.info("Incoming request to get customer account statement: {} from ip {}", accountStatementRequest, request.getRemoteAddr());

        if (accountStatementRequest.getCustomerId() == null || accountStatementRequest.getCustomerId().isEmpty()
                || accountStatementRequest.getAccountNumber() == null || accountStatementRequest.getAccountNumber().isEmpty()) {
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data(null)
                    .build();
        }

        AppResponse<?> response = accountService.generateAccountStatement(accountStatementRequest.getCustomerId(), accountStatementRequest.getAccountNumber(),
                accountStatementRequest.getStartDate(),  accountStatementRequest.getEndDate());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Account", description = "API endpoint to get delete account")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/delete-account")
    public ResponseEntity<AppResponse<?>> deleteAccount(@RequestBody DeleteAccountRequest deleteAccountRequest, HttpServletRequest request) {
        log.info("Incoming request to delete account wih id {} from ip {}", deleteAccountRequest.getAccountId(), request.getRemoteAddr());

        if (deleteAccountRequest.getCustomerId() == null || deleteAccountRequest.getCustomerId().isEmpty()
            || deleteAccountRequest.getAccountId() == null || deleteAccountRequest.getAccountId().isEmpty()) {
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data(null)
                    .build();
        }
        deleteAccountRequest.setIpAddress(request.getRemoteAddr());
        AppResponse<?> response = accountService.deleteAccount(deleteAccountRequest);

        return ResponseEntity.ok(response);
    }
}
