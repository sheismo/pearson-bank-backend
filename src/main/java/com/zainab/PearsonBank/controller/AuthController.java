package com.zainab.PearsonBank.controller;

import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.dto.ChangePasswordPinRequest;
import com.zainab.PearsonBank.dto.SetPasswordPinRequest;
import com.zainab.PearsonBank.service.AuthService;
import com.zainab.PearsonBank.utils.AccountHelper;
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
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "Authentication & Credentials Management APIs")
public class AuthController {
    @Autowired
    AuthService authService;

    @Autowired
    AccountHelper accountHelper;

    @Operation(summary = "Set App Password", description = "API endpoint to set user app password for first time users")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/set-password")
    public ResponseEntity<AppResponse<?>> setPassword(@RequestBody SetPasswordPinRequest setPasswordRequest, HttpServletRequest request) {
        log.info("Incoming request to set app password for customer from ip {}", request.getRemoteAddr());
        AppResponse<?> response = null;

        String customerId = setPasswordRequest.getCustomerId();
        String appPassword = setPasswordRequest.getPassPin();
        setPasswordRequest.setIpAddress(request.getRemoteAddr());

        if (appPassword == null || appPassword.isEmpty() ||  customerId == null || customerId.isEmpty() || !accountHelper.isFirstTimeLogin(customerId) ) {
            log.error("Invalid Request::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }

        if (appPassword.length() < 8) { // password must be 8 or more characters
            log.error("Invalid Request::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Password length is invalid");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String res = authService.setAppPassword(customerId, appPassword);
            response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), res);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Failed to set app password: " + e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "Change App Password", description = "API endpoint to change user app password")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/change-password")
    public ResponseEntity<AppResponse<?>> changePassword(@RequestBody ChangePasswordPinRequest changePasswordRequest, HttpServletRequest request) {
        log.info("Incoming request to change app password for customer from ip {}", request.getRemoteAddr());
        AppResponse<?> response = null;

        String customerId = changePasswordRequest.getCustomerId();
        String oldPassword = changePasswordRequest.getOldPassPin();
        String newPassword = changePasswordRequest.getNewPassPin();
        changePasswordRequest.setIpAddress(request.getRemoteAddr());

        if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty() ||
                customerId == null || customerId.isEmpty() || accountHelper.isFirstTimeLogin(customerId) ) {
            log.error("Invalid Request - Empty parameters::::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Empty parameters!");
            return ResponseEntity.badRequest().body(response);
        }

        if (oldPassword.length() < 8 || newPassword.length() < 8) { // password must be 8 or more characters
            log.error("Invalid Request - length is less than 8::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Password length is invalid!");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String res = authService.changeAppPassword(customerId, oldPassword, newPassword);
            response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), res);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Failed to set app password: " + e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "Set Transaction Pin", description = "API endpoint to set user transaction pin for first time users")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/set-pin")
    public ResponseEntity<AppResponse<?>> setPin(@RequestBody SetPasswordPinRequest setPinRequest, HttpServletRequest request) {
        log.info("Incoming request to set transaction pin for customer from ip{}", request.getRemoteAddr());
        AppResponse<?> response = null;

        String customerId = setPinRequest.getCustomerId();
        String transactionPin = setPinRequest.getPassPin();
        setPinRequest.setIpAddress(request.getRemoteAddr());

        if (transactionPin == null || transactionPin.isEmpty() ||  customerId == null || customerId.isEmpty() || !accountHelper.isFirstTimeLogin(customerId)) {
            log.error("Invalid Request:::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Invalid Request");
            return ResponseEntity.badRequest().body(response);
        }

        if (transactionPin.length() != 4) { // pin must be exactly 4 characters
            log.error("Invalid Request:::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Pin length is invalid");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String res = authService.setTransactionPin(customerId, transactionPin);
            response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), res);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Failed to set transaction pin: " + e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "Change Transaction Pin", description = "API endpoint to change user transaction pin")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/change-pin")
    public ResponseEntity<AppResponse<?>> changePin(@RequestBody ChangePasswordPinRequest setPinRequest, HttpServletRequest request) {
        log.info("Incoming request to change transaction pin for customer from ip{}", request.getRemoteAddr());
        AppResponse<?> response = null;

        String customerId = setPinRequest.getCustomerId();
        String oldTransactionPin = setPinRequest.getOldPassPin();
        String newTransactionPin = setPinRequest.getNewPassPin();
        setPinRequest.setIpAddress(request.getRemoteAddr());

        if (oldTransactionPin == null || oldTransactionPin.isEmpty() ||  newTransactionPin == null || newTransactionPin.isEmpty() ||
                customerId == null || customerId.isEmpty() || accountHelper.isFirstTimeLogin(customerId)) {
            log.error("Invalid Request - empty parameters:::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Invalid Request");
            return ResponseEntity.badRequest().body(response);
        }

        if (oldTransactionPin.length() != 4 || newTransactionPin.length() != 4) { // pin must be exactly 4 characters
            log.error("Invalid Request - pin length is invalid:::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Pin length is invalid");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String res = authService.changeTransactionPin(customerId, oldTransactionPin, newTransactionPin);
            response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), res);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Failed to set transaction pin: " + e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }

//    @Operation(summary = "Login", description = "API endpoint to login user ")
//    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
//    @PostMapping("/login")
//    public ResponseEntity<AppResponse<?>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
//        log.info("Incoming request to login user from ip{}", request.getRemoteAddr());
//        AppResponse<?> response = null;
//
//        String accountNumber = loginRequest.getAccountNumber();
//        String appPassword = loginRequest.getAppPassword();
//        loginRequest.setIpAddress(request.getRemoteAddr());
//
//        if (accountNumber == null || accountNumber.isEmpty() ||  appPassword == null || appPassword.isEmpty()) {
//            log.error("Invalid Request - empty parameters::::::");
//            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Invalid Request");
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        try {
//            String res = authService.login();
//            response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), res);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            response = AppResponse.builder()
//                    .responseCode(AccountResponses.FAILED.getCode())
//                    .responseMessage("Failed to login: " + e.getMessage())
//                    .data(null)
//                    .build();
//            return ResponseEntity.internalServerError().body(response);
//        }
//    }


}

