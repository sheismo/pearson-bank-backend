package com.zainab.PearsonBank.service;

import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.dto.CreditDebitRequest;
import com.zainab.PearsonBank.dto.TransferRequest;

public interface TransactionService {
    AppResponse creditAccount(CreditDebitRequest creditRequest);
    AppResponse debitAccount(CreditDebitRequest creditRequest);
    AppResponse transferMoney(TransferRequest transferRequest);
}
