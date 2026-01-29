package com.zainab.PearsonBank.service;

import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.dto.CreditDebitRequest;
import com.zainab.PearsonBank.dto.TransactionDetails;
import com.zainab.PearsonBank.dto.TransferRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TransactionService {
    AppResponse<?> creditAccount(CreditDebitRequest creditRequest);
    AppResponse<?> debitAccount(CreditDebitRequest creditRequest);
    AppResponse<?> transferMoney(TransferRequest transferRequest);
    TransactionDetails getSingleTransaction(String customerId, String accountNumber, String transactionId) throws Exception;
    List<TransactionDetails> getTransactionsForCustomer(String customerId, String  accountNumber) throws Exception;
    List<TransactionDetails> getTransactionsForCustomer(String customerId, String  accountNumber, String startDate, String endDate) throws Exception;
    ResponseEntity<?> generateReceiptPdf(String transactionId);
}
