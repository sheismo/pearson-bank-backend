package com.zainab.PearsonBank.controller;

import com.zainab.PearsonBank.dto.AccountDetails;
import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.dto.CustomerDetails;
import com.zainab.PearsonBank.dto.TransactionDetails;
import com.zainab.PearsonBank.service.AdminService;
import com.zainab.PearsonBank.utils.AccountResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Slf4j
@Tag(name = "Admin Management APIs")
public class AdminController {
    @Autowired
    AdminService adminService;

    @Operation(summary = "Get All Accounts", description = "API endpoint to get all accounts")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @GetMapping("/get-all-accounts")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<?>> getAccounts(HttpServletRequest request) {
        log.info("Incoming request to get all user accounts from ip {}", request.getRemoteAddr());

        List<AccountDetails> accountList = adminService.getAllAccounts();
        AppResponse<?> response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), accountList);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Single Account", description = "API endpoint to get single account")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @GetMapping("/get-single-account/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<?>> getSingleAccount(@PathVariable("id") String accountId, HttpServletRequest request) {
        log.info("Incoming request to get single user account from ip {}", request.getRemoteAddr());

        AccountDetails account = adminService.getSingleAccount(accountId);
        AppResponse<?> response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), account);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate Account", description = "API endpoint to activate account")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @GetMapping("/activate-account/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<?>> activateAccount(@PathVariable("id") String accountId, HttpServletRequest request) {
        log.info("Incoming request to activate user account from ip {}", request.getRemoteAddr());

        boolean result = adminService.activateAccount(accountId);
        AppResponse<?> response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), result);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deactivate Account", description = "API endpoint to deactivate account")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @GetMapping("/deactivate-account/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<?>> deactivateAccount(@PathVariable("id") String accountId, HttpServletRequest request) {
        log.info("Incoming request to deactivate user account from ip {}", request.getRemoteAddr());

        boolean result = adminService.deactivateAccount(accountId);
        AppResponse<?> response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), result);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get All Customers", description = "API endpoint to get all customers")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @GetMapping("/get-all-customers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<?>> getCustomers(HttpServletRequest request) {
        log.info("Incoming request to get all customers from ip {}", request.getRemoteAddr());

        List<CustomerDetails> customerList = adminService.getAllCustomers();
        AppResponse<?> response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), customerList);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Single Customer", description = "API endpoint to get single customer")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @GetMapping("/get-single-customer/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<?>> getSingleCustomer(@PathVariable("id") String customerId, HttpServletRequest request) {
        log.info("Incoming request to get single customers from ip {}", request.getRemoteAddr());

        CustomerDetails customer = adminService.getSingleCustomer(customerId);
        AppResponse<?> response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), customer);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Enable Customer's Profile", description = "API endpoint to enable customer's profile")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @GetMapping("/enable-customer/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<?>> enableCustomerProfile(@PathVariable String customerId, HttpServletRequest request) {
        log.info("Incoming request to enable customer's profile from ip {}", request.getRemoteAddr());

        boolean result = adminService.enableUser(customerId);
        AppResponse<?> response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), result);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Disable Customer's Profile", description = "API endpoint to disable customer's profile")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @GetMapping("/disable-customer/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<?>> disableCustomerProfile(@PathVariable("id") String customerId, HttpServletRequest request) {
        log.info("Incoming request to disable customer's profile from ip {}", request.getRemoteAddr());

        boolean result = adminService.disableUser(customerId);
        AppResponse<?> response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), result);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get All Transactions", description = "API endpoint to get all transactions")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @GetMapping("/get-all-transactions")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<?>> getTransactions(HttpServletRequest request) {
        log.info("Incoming request to get all transactions from ip {}", request.getRemoteAddr());

        List<TransactionDetails> transactionList = adminService.getAllTransactions();
        AppResponse<?> response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), transactionList);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Single Transaction", description = "API endpoint to get single transaction")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @GetMapping("/get-single-transaction/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<?>> getSingleTransaction(@PathVariable("id") String transactionId, HttpServletRequest request) {
        log.info("Incoming request to get single transaction from ip {}", request.getRemoteAddr());

        TransactionDetails transaction = adminService.getSingleTransaction(transactionId);
        AppResponse<?> response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), transaction);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reverse Transaction", description = "API endpoint to reverse transaction")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @GetMapping("/reverse-transaction/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<?>> reverseTransaction(@PathVariable("id") String transactionId, HttpServletRequest request) {
        log.info("Incoming request tore reverse transaction from ip {}", request.getRemoteAddr());

        boolean result = adminService.reverseTransaction(transactionId);
        AppResponse<?> response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), result);
        return ResponseEntity.ok(response);
    }
}
