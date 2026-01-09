package com.zainab.PearsonBank.service;

import com.zainab.PearsonBank.dto.AccountDetails;
import com.zainab.PearsonBank.dto.CustomerDetails;
import com.zainab.PearsonBank.dto.TransactionDetails;

import java.util.List;

public interface AdminService {
    List<AccountDetails> getAllAccounts();
    AccountDetails getSingleAccount(String accountId);
    boolean activateAccount(String accountId);
    boolean deactivateAccount(String accountId);

    List<CustomerDetails> getAllCustomers();
    CustomerDetails getSingleCustomer(String customerId);
    boolean disableUser(String customerId);
    boolean enableUser(String customerId);

    List<TransactionDetails> getAllTransactions();
    TransactionDetails getSingleTransaction(String transactionId);
    boolean reverseTransaction(String transactionId);
}
