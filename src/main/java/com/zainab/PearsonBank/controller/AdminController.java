package com.zainab.PearsonBank.controller;

import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.service.AdminService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Slf4j
@Tag(name = "Admin Management APIs")
public class AdminController {
    @Autowired
    AdminService adminService;

    @Operation(summary = "Get Customer Account", description = "API endpoint to get a customer account")
    @ApiResponse(responseCode = "200", description = "Request processed successfully!")
    @PostMapping("/get-account")
    public ResponseEntity<AppResponse<?>> getCustomerAccount(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        log.info("Incoming request to get customer account from ip {}", request.getRemoteAddr());
        String accountId = payload.get("accountId");
        String channel = payload.get("channel");
        log.info("Account is in request is: {} - channel is {}", accountId, channel);

        if (accountId == null || accountId.isEmpty()) {
            AppResponse<?> response = AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data(null)
                    .build();
        }

        AppResponse<?> response = adminService.
        return ResponseEntity.ok(response);
    }
}
