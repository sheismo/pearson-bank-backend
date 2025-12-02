package com.zainab.PearsonBank.service;

import com.zainab.PearsonBank.entity.Account;
import com.zainab.PearsonBank.entity.Customer;
import com.zainab.PearsonBank.entity.Transaction;

import java.util.List;

public interface AdminService {
    List<Account> getAllAccounts();
    Account getSingleAccount(String accountId);
    boolean activateAccount(String accountId);
    boolean deactivateAccount(String accountId);

    List<Customer> getAllCustomers();
    Customer getSingleCustomer(String customerId);
    boolean disableUser(String customerId);
    boolean enableUser(String customerId);

    List<Transaction> getAllTransactions();
    Transaction getSingleTransaction(String transactionId);
    boolean reverseTransaction(String transactionId);
}
