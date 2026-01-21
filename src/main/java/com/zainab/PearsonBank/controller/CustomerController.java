package com.zainab.PearsonBank.controller;

import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.dto.CustomerRequest;
import com.zainab.PearsonBank.dto.EnquiryRequest;
import com.zainab.PearsonBank.service.CustomerService;
import com.zainab.PearsonBank.utils.AccountResponses;
import com.zainab.PearsonBank.utils.AccountUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@Slf4j
@Tag(name = "Customer Management APIs")
public class CustomerController {
    @Autowired
    CustomerService customerService;

    /**
     * @return AppResponse containing info about the api operation
     */
    @Operation(summary = "Onboard New Customer", description = "API endpoint to create new account for a new customer")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/onboard")
    public ResponseEntity<AppResponse<?>> onboardCustomer(@RequestBody CustomerRequest customerRequest, HttpServletRequest request) {
        log.info("Incoming request to create new user account: {} from ip {}", customerRequest, request.getRemoteAddr());
        AppResponse<?> response = null;

        if (!AccountUtils.validateCustomerRequest(customerRequest)) {
            log.error("Invalid Request::::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
        try {
            response = customerService.onboardNewCustomer(customerRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error occurred: {}", e.getMessage());
            response = AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("User Onboarding Failed: please try again later or contact system admin!")
                    .data(null)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "Verify User Email", description = "API endpoint to verify user email ")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/verify-email")
    public ResponseEntity<AppResponse<?>> verifyCustomerEmail(@RequestBody Map<String, String> verifyEmailRequest, HttpServletRequest request) {
        log.info("Incoming request to verify email for new user from ip {}", request.getRemoteAddr());
        AppResponse<?> response = null;

        String emailAddress = verifyEmailRequest.get("email");
        String otp = verifyEmailRequest.get("otp");

        if (emailAddress == null || emailAddress.isEmpty() || otp == null || otp.isEmpty() ) {
            log.error("Invalid Request - Empty parameters::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Otp is incorrect!");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            response = customerService.verifyCustomerEmail(emailAddress, otp);
            return ResponseEntity.ok(response);
        } catch(Exception e) {
            response = AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("User Onboarding Failed: " + e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "Create Account For User", description = "API endpoint to create account for user")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/create-account")
    public ResponseEntity<AppResponse<?>> createAccount(@RequestBody Map<String, String> createAccountRequest, HttpServletRequest request) {
        log.info("Incoming request to create new user account from ip {}", request.getRemoteAddr());
        AppResponse<?> response = null;

        String emailAddress = createAccountRequest.get("email");

        if (emailAddress == null || emailAddress.isEmpty() ) {
            log.error("Invalid Request:::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            response = customerService.createAccountForCustomer(emailAddress);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.info("Account Creation Failed: {}", e.getMessage(), e.getCause());
            e.printStackTrace();

            response = AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Error occurred: Account Creation Failed")
                    .data(null)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "Balance Enquiry", description = "API endpoint to check user account balance")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PreAuthorize("hasRole('ROLE_CUSTOMER') and @accountSecurity.accountBelongsToUser(authentication, #enquiryRequest.accountNumber) ")
    @PostMapping("/balance-enquiry")
    public ResponseEntity<AppResponse<?>> getCustomerBalance(@RequestBody EnquiryRequest enquiryRequest, HttpServletRequest request) {
        log.info("Incoming request to get account balance: {} from ip {}", enquiryRequest, request.getRemoteAddr());

        if (!AccountUtils.validateEnquiryRequest(enquiryRequest)) {
            log.error("Invalid Request::::");
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        AppResponse<?> response = customerService.balanceEnquiry(enquiryRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Name Enquiry", description = "API endpoint to get user account name")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/name-enquiry")
    public ResponseEntity<AppResponse<Object>> getCustomerName(@RequestBody EnquiryRequest enquiryRequest, HttpServletRequest request) {
        log.info("Incoming request to get account name: {} from ip {}", enquiryRequest, request.getRemoteAddr());

        if (!AccountUtils.validateEnquiryRequest(enquiryRequest)) {
            log.error("Invalid Request:::::");
            AppResponse<Object> response = AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        String accountName = customerService.nameEnquiry(enquiryRequest);
        AppResponse<Object> response = AppResponse.builder()
                .responseCode(AccountResponses.SUCCESS.getCode())
                .responseMessage(AccountResponses.SUCCESS.getMessage())
                .data(accountName)
                .build();
        return ResponseEntity.ok(response);
    }
}
