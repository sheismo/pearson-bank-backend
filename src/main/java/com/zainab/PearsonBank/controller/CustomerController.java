package com.zainab.PearsonBank.controller;

import com.zainab.PearsonBank.dto.*;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer")
@Slf4j
@Tag(name = "User Management APIs")
public class CustomerController {
    @Autowired
    CustomerService customerService;

    /**
     * @return AppResponse containing info about newly created account or errors if any
     */
    @Operation(
            summary = "Create new user account", description = "API endpoint to create new account for a new user/customer"
    )
    @ApiResponse(
            responseCode = "200", description = "Request processed successfully!"
    )
    @PostMapping("/open-account")
    public ResponseEntity<AppResponse<?>> createAccount(@RequestBody CustomerRequest customerRequest, HttpServletRequest request) {
        log.info("Incoming request to create new customer account: {} from ip {}", customerRequest, request.getRemoteAddr());

        if (!AccountUtils.validateCustomerRequest(customerRequest)) {
            log.error("Invalid Request:::");
            return ResponseEntity.badRequest().body(new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), null));
        }
        AppResponse<?> response = customerService.createAccount(customerRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Balance Enquiry", description = "API endpoint to check customer account balance")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
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

    @Operation(summary = "Name Enquiry", description = "API endpoint to get customer account name")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
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
