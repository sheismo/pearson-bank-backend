package com.zainab.PearsonBank.service;

import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.dto.DeleteAccountRequest;
import com.zainab.PearsonBank.dto.GetAccountsRequest;

public interface AccountService {
    AppResponse<?> getAccount(String accountId);
    AppResponse<?> getAccounts(GetAccountsRequest request);
    AppResponse<?> generateAccountStatement(String customerId, String accountNumber, String startDate, String endDate);
    AppResponse<?> deleteAccount(DeleteAccountRequest deleteAccountRequest);
}
