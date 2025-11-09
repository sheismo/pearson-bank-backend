package com.zainab.PearsonBank.controller;

import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.dto.TransactionPinRequest;
import com.zainab.PearsonBank.utils.AccountResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/credential")
@Slf4j
@Tag(name = "Credentials Management APIs")
public class CredentialController {

    @Operation(summary = "Set User Password", description = "API endpoint to set user app password")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/set-user-password")
    public ResponseEntity<AppResponse<?>> setUserPassword(@RequestBody TransactionPinRequest transactionPinRequest, HttpServletRequest request) {
        log.info("Incoming request to set app password for customer from ip{}", request.getRemoteAddr());

        // TODO get logged-in user details
        // check if the customer id passed is same as customer id of the logged-in customer
        // check if customer in the request owns the account passed
        //        boolean isLoggedInCustomerMakingRequest; request.getCustomerId().equals(loggedInCustomerId)

        // todo parameters are: customer id, password, channel, ip
        AppResponse<?> response = AppResponse.builder()
                .responseCode(AccountResponses.FAILED.getCode())
                .responseMessage(AccountResponses.FAILED.getMessage())
                .data(null)
                .build();

        return ResponseEntity.internalServerError().body(response);

    }

    @Operation(summary = "Confirm User Password", description = "API endpoint to confirm user password")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/confirm-user-password")
    public ResponseEntity<AppResponse<?>> confirmUserPassword(@RequestBody TransactionPinRequest transactionPinRequest, HttpServletRequest request) {
        log.info("Incoming request to confirm app password for customer from ip:: {}", request.getRemoteAddr());

        // TODO get logged-in user details
        // check if the customer id passed is same as customer id of the logged-in customer
        // check if customer in the request owns the account passed
        //        boolean isLoggedInCustomerMakingRequest; request.getCustomerId().equals(loggedInCustomerId)

        // todo parameters are: customer id, pin, source/channel
        AppResponse<?> response = AppResponse.builder()
                .responseCode(AccountResponses.FAILED.getCode())
                .responseMessage(AccountResponses.FAILED.getMessage())
                .data(null)
                .build();

        return ResponseEntity.internalServerError().body(response);

    }
}

// TODO the above should be methods inside Credentials Service,however they would be called from other controllers as needed
