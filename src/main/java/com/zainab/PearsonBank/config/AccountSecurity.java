package com.zainab.PearsonBank.config;

import com.zainab.PearsonBank.repository.AccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("accountSecurity")
public class AccountSecurity {
    private AccountRepository accountRepository;
    private Authentication authentication;

    public boolean accountBelongsToUser(Authentication authentication, String accountNumber) {
        String email = authentication.getName();
        return accountRepository.existsByAccountNumberAndCustomerEmail(accountNumber, email);
    }
}
