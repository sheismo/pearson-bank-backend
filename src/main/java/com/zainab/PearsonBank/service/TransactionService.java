package com.zainab.PearsonBank.service;

import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.dto.CreditDebitRequest;
import com.zainab.PearsonBank.dto.TransferRequest;
import com.zainab.PearsonBank.entity.Transaction;

import java.util.List;

public interface TransactionService {
    AppResponse<?> creditAccount(CreditDebitRequest creditRequest);
    AppResponse<?> debitAccount(CreditDebitRequest creditRequest);
    AppResponse<?> transferMoney(TransferRequest transferRequest);
    Transaction getSingleTransaction(String customerId, String accountNumber, String transactionId);
    List<Transaction> getTransactionsForCustomer(String customerId, String  accountNumber);
    List<Transaction> getTransactionsForCustomer(String customerId, String  accountNumber, String startDate, String endDate);
}
