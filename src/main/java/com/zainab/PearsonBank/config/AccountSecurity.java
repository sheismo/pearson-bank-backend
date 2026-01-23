package com.zainab.PearsonBank.config;

import com.zainab.PearsonBank.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("accountSecurity")
public class AccountSecurity {
    @Autowired
    private AccountRepository accountRepository;

    public boolean accountBelongsToUser(Authentication authentication, String accountNumber) {
        String email = authentication.getName();
        return accountRepository.existsByAccountNumberAndUser_Email(accountNumber, email);
    }
}
