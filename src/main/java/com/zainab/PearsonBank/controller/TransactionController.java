package com.zainab.PearsonBank.controller;

import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.dto.CreditDebitRequest;
import com.zainab.PearsonBank.dto.TransferRequest;
import com.zainab.PearsonBank.service.TransactionService;
import com.zainab.PearsonBank.utils.AccountHelper;
import com.zainab.PearsonBank.utils.AccountResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        if (transferRequest.getCrAccountNumber().isEmpty() || transferRequest.getDrAccountNumber().isEmpty()
            || !accountHelper.checkIfAmountIsValid(transferRequest.getAmount()) || String.valueOf(transferRequest.getCustomerId()).isEmpty()) {

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
}
