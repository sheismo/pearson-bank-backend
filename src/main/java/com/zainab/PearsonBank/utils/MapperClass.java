package com.zainab.PearsonBank.utils;

import com.zainab.PearsonBank.dto.AccountDetails;
import com.zainab.PearsonBank.dto.CustomerDetails;
import com.zainab.PearsonBank.dto.TransactionDetails;
import com.zainab.PearsonBank.entity.Account;
import com.zainab.PearsonBank.entity.Transaction;
import com.zainab.PearsonBank.entity.User;
import org.springframework.stereotype.Component;

@Component
public class MapperClass {
    private final AccountHelper accountHelper;

    public MapperClass(AccountHelper accountHelper) {
        this.accountHelper = accountHelper;
    }

    public AccountDetails getAccountDetails(Account account) {
        AccountDetails dto = new AccountDetails();
        dto.setAccountId(account.getId());
        dto.setOwnerId(account.getUser().getId());
        dto.setAccountName(accountHelper.getCustomerFullName(account.getUser()));
        dto.setAccountNumber(account.getAccountNumber());
        dto.setAccountBalance(account.getAccountBalance());
        dto.setAccountCurrency(account.getAccountCurrency());
        dto.setAccountStatus(account.getAccountStatus());
        dto.setLinkedEmail(account.getUser().getEmail());
        dto.setLinkedPhone(account.getUser().getPhoneNumber());

        return dto;
    }

    public CustomerDetails getCustomerDetails(User user) {
        CustomerDetails dto = new CustomerDetails();
        dto.setId(user.getId());
        dto.setFullName(accountHelper.getCustomerFullName(user));
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAlternativePhoneNumber(user.getAlternativePhoneNumber());
        dto.setGender(user.getGender());
        dto.setAddress(user.getAddress());
        dto.setLocation(user.getState() + ", " + user.getCountry());
        dto.setNoOfAccounts(user.getNoOfAccounts());
        if (!user.getAccounts().isEmpty()) {
            dto.setPrimaryAccountNumber(user.getAccounts().get(0).getAccountNumber());
        }
        dto.setTotalBalance(user.getTotalBalance());
        dto.setProfileEnabled(user.isProfileEnabled());

        return dto;
    }

    public CustomerDetails getCustomerDetails(User user, String primaryAccount) {
        CustomerDetails dto = new CustomerDetails();
        dto.setId(user.getId());
        dto.setFullName(accountHelper.getCustomerFullName(user));
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAlternativePhoneNumber(user.getAlternativePhoneNumber());
        dto.setGender(user.getGender());
        dto.setAddress(user.getAddress());
        dto.setLocation(user.getState() + ", " + user.getCountry());
        dto.setNoOfAccounts(user.getNoOfAccounts());
        dto.setPrimaryAccountNumber(primaryAccount);
        dto.setTotalBalance(user.getTotalBalance());
        dto.setProfileEnabled(user.isProfileEnabled());

        return dto;
    }

    public TransactionDetails getTransactionDetails(Transaction transaction) {
        TransactionDetails dto = new TransactionDetails();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setSenderName(transaction.getDrAccountName());
        dto.setSenderAccount(transaction.getDrAccountNumber());
        dto.setBeneficiaryName(transaction.getCrAccountName());
        dto.setBeneficiaryAccount(transaction.getCrAccountNumber());
        dto.setAmount(transaction.getAmount());
        dto.setDate(transaction.getCreatedDate());
        dto.setChannel(transaction.getChannel());
        dto.setType(String.valueOf(transaction.getType()));
        dto.setNarration(transaction.getNarration());
        dto.setReferenceNo(transaction.getReferenceNo());
        dto.setStatus(transaction.getTransactionStatus().name());

        return dto;
    }
}
