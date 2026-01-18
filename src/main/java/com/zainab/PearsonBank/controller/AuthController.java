package com.zainab.PearsonBank.controller;

import com.zainab.PearsonBank.dto.*;
import com.zainab.PearsonBank.service.AuthService;
import com.zainab.PearsonBank.utils.AccountHelper;
import com.zainab.PearsonBank.utils.AccountResponses;
import com.zainab.PearsonBank.utils.PasswordGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "Authentication & Credentials Management APIs")
public class AuthController {
    @Autowired
    AuthService authService;

    @Autowired
    AccountHelper accountHelper;

    @Autowired
    PasswordGenerator passwordGenerator;

    @Operation(summary = "Login", description = "API endpoint to login user ")
    @ApiResponse(responseCode = "200", description = "Login successful!")
    @PostMapping("/login")
    public ResponseEntity<AppResponse<?>> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        log.info("Incoming request to login user from ip{}", request.getRemoteAddr());
        AppResponse<?> response = null;

        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        loginRequest.setIpAddress(ipAddress);
        loginRequest.setUserAgent(userAgent);

        try {
            JwtResponse res = authService.authenticateUser(loginRequest);
            response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), res);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to login - {}", e.getMessage());
            response = AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Failed to login!")
                    .data(null)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "Set App Password", description = "API endpoint to set user app password for first time users")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/set-password")
    public ResponseEntity<AppResponse<?>> setPassword(@RequestBody SetPasswordPinRequest setPasswordRequest, HttpServletRequest request) {
        log.info("Incoming request to set app password for user from ip {}", request.getRemoteAddr());
        AppResponse<?> response = null;

        String customerId = setPasswordRequest.getCustomerId();
        String appPassword = setPasswordRequest.getPassPin();
        String channel = setPasswordRequest.getChannel();
        setPasswordRequest.setIpAddress(request.getRemoteAddr());

        if (appPassword == null || appPassword.isEmpty() ||  customerId == null || customerId.isEmpty() || accountHelper.hasSetAppPassword(customerId) ) {
            log.error("Invalid Request - Issue with input fields or user already set password::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }

        if (appPassword.length() < 12 || !passwordGenerator.isValidPassword(appPassword)) { // password must be 12 or more characters, uppercase, lowercase, digit, symbol)
            log.error("Invalid Request::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage() + " - Password length is invalid",
                    "Password must be at least 12 characters,and should contain at least 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 symbol.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String res = authService.setAppPassword(customerId, appPassword);
            response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), res);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to set app password {}", e.getMessage());
            response = AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Failed to set app password")
                    .data(null)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "Change App Password", description = "API endpoint to change user app password")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/change-password")
    public ResponseEntity<AppResponse<?>> changePassword(@RequestBody ChangePasswordPinRequest changePasswordRequest, HttpServletRequest request) {
        log.info("Incoming request to change app password for user from ip {}", request.getRemoteAddr());
        AppResponse<?> response = null;

        String customerId = changePasswordRequest.getCustomerId();
        String oldPassword = changePasswordRequest.getOldPassPin();
        String newPassword = changePasswordRequest.getNewPassPin();
        String channel = changePasswordRequest.getChannel();
        changePasswordRequest.setIpAddress(request.getRemoteAddr());

        if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty() ||
                customerId == null || customerId.isEmpty() || !accountHelper.hasSetAppPassword(customerId) ) {
            log.error("Invalid Request - Empty parameters::::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Empty parameters!");
            return ResponseEntity.badRequest().body(response);
        }

        if (oldPassword.equals(newPassword)) {
            log.error("Invalid Request - Old Password is the same as New Password::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Old Password cannot be the same as New Password!");
            return ResponseEntity.badRequest().body(response);
        }

        if (newPassword.length() < 12 || !passwordGenerator.isValidPassword(newPassword)) { // password must be 8 or more characters
            log.error("Invalid Request - password does not meet complexity requirements::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Password does not meet complexity requirements!");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String res = authService.changeAppPassword(customerId, oldPassword, newPassword);
            response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), res);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to change app password: {} ", e.getMessage());
            response = AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Failed to change app password")
                    .data(null)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "Set Transaction Pin", description = "API endpoint to set user transaction pin for first time users")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PostMapping("/set-pin")
    public ResponseEntity<AppResponse<?>> setPin(@RequestBody SetPasswordPinRequest setPinRequest, HttpServletRequest request) {
        log.info("Incoming request to set transaction pin for user from ip{}", request.getRemoteAddr());
        AppResponse<?> response = null;

        String customerId = setPinRequest.getCustomerId();
        String transactionPin = setPinRequest.getPassPin();
        String channel = setPinRequest.getChannel();
        setPinRequest.setIpAddress(request.getRemoteAddr());

        if (transactionPin == null || transactionPin.isEmpty() ||  customerId == null || customerId.isEmpty() || accountHelper.hasSetTransactionPin(customerId)) {
            log.error("Invalid Request:::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Invalid Request");
            return ResponseEntity.badRequest().body(response);
        }

        if (transactionPin.length() != 4 || !passwordGenerator.isValidPin(transactionPin)) { // pin must be exactly 4 characters
            log.error("Invalid Request - Pin must be 4 digits:::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Pin must be 4 digits");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String res = authService.setTransactionPin(customerId, transactionPin);
            response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), res);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to set transaction pin: {}", e.getMessage());
            response = AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Failed to set transaction pin: ")
                    .data(null)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "Change Transaction Pin", description = "API endpoint to change user transaction pin")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PostMapping("/change-pin")
    public ResponseEntity<AppResponse<?>> changePin(@RequestBody ChangePasswordPinRequest changePinRequest, HttpServletRequest request) {
        log.info("Incoming request to change transaction pin for user from ip{}", request.getRemoteAddr());
        AppResponse<?> response = null;

        String customerId = changePinRequest.getCustomerId();
        String oldTransactionPin = changePinRequest.getOldPassPin();
        String newTransactionPin = changePinRequest.getNewPassPin();
        String channel = changePinRequest.getChannel();
        changePinRequest.setIpAddress(request.getRemoteAddr());

        if (oldTransactionPin == null || oldTransactionPin.isEmpty() ||  newTransactionPin == null || newTransactionPin.isEmpty() ||
                customerId == null || customerId.isEmpty() || !accountHelper.hasSetTransactionPin(customerId)) {
            log.error("Invalid Request - empty parameters:::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Invalid Request - empty parameters");
            return ResponseEntity.badRequest().body(response);
        }

        if (oldTransactionPin.equals(newTransactionPin)) {
            log.error("Invalid Request - Old Pin cannot be the same as New Pin:::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Old Pin cannot be the same as New Pin!");
            return ResponseEntity.badRequest().body(response);
        }

        if (newTransactionPin.length() != 4 || !passwordGenerator.isValidPin(newTransactionPin)) {
            log.error("Invalid Request - Pin does not meet complexity requirement:::::");
            response = new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), AccountResponses.INVALID_REQUEST.getMessage(), "Pin does not meet complexity requirement");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String res = authService.changeTransactionPin(customerId, oldTransactionPin, newTransactionPin);
            response = new AppResponse<>(AccountResponses.SUCCESS.getCode(), AccountResponses.SUCCESS.getMessage(), res);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to change transaction pin: {}", e.getMessage());
            response = AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Failed to change transaction pin")
                    .data(null)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "Forgot Password", description = "API endpoint for forgot password ")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/forgot-password")
    public ResponseEntity<AppResponse<?>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest, HttpServletRequest request) {
        log.info("Incoming request for forgot password from ip{}", request.getRemoteAddr());

        try {
            authService.forgotPassword(forgotPasswordRequest);
            return ResponseEntity.ok(new AppResponse<>("00", "Password reset mail has been sent!", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AppResponse<>("40", e.getMessage(), "null"));
        }
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        log.info("Incoming request to validate reset token::");

        return ResponseEntity.ok().body(authService.validateResetToken(token));
    }

    @Operation(summary = "Reset Password", description = "API endpoint to reset password")
    @ApiResponse(responseCode = "200", description = "Password reset successfully!")
    @PostMapping("/reset-password")
    public ResponseEntity<AppResponse<?>> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
        log.info("Incoming request to reset password from ip {}", request.getRemoteAddr());

        try {
            authService.resetPassword(resetPasswordRequest);
            return ResponseEntity.ok(new AppResponse<>("00", "Password reset successfully!", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AppResponse<>("40", e.getMessage(), null));
        }
    }

    @Operation(summary = "Refresh Token", description = "API endpoint to refresh token")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> refreshTokenRequest, HttpServletRequest request) {
        log.info("Incoming request to refresh access token from ip {}::", request.getRemoteAddr());

        try {
            String refreshToken = refreshTokenRequest.get("refreshToken");
            return authService.generateRefreshToken(refreshToken);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AppResponse<>("40", e.getMessage(), null));
        }
    }

    @Operation(summary = "Logout", description = "API endpoint to logout")
    @ApiResponse(responseCode = "200", description = "Logged out successfully!")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return authService.logout(request);
    }

    @Operation(summary = "Logout All Device", description = "API endpoint to logout of all devices")
    @ApiResponse(responseCode = "200", description = "Logged out of all devices successfully!")
    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAllDevices(HttpServletRequest request) {
        return authService.logoutAllDevices(request);
    }

    private String getClientIp(HttpServletRequest request) {

        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }

        return request.getRemoteAddr();
    }


}

