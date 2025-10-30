package com.zainab.PearsonBank.controller;

import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.dto.TransactionPinRequest;
import com.zainab.PearsonBank.utils.AccountResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer")
@Slf4j
@Tag(name = "Credentials Management APIs")
public class CredentialController {

    @PostMapping("/set-app-password")
    public ResponseEntity<AppResponse<?>> setUserPassword(@RequestBody TransactionPinRequest transactionPinRequest, HttpServletRequest request) {
        log.info("Incoming request to confirm transaction pin for customer from ip {}", request.getRemoteAddr());

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

    @PostMapping("/confirm-app-password")
    public ResponseEntity<AppResponse<?>> confirmUserPassword(@RequestBody TransactionPinRequest transactionPinRequest, HttpServletRequest request) {
        log.info("Incoming request to confirm transaction pin for customer from ip {}", request.getRemoteAddr());

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

// watch video 370, do set pin and confirm pin features pin, watch the remaining two youtube videos