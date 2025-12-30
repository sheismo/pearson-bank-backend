package com.zainab.PearsonBank.security;

import com.zainab.PearsonBank.entity.Account;
import com.zainab.PearsonBank.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AccountSecurity {
    @Autowired
    private AccountRepository accountRepository;

    public boolean accountBelongsToUser(Authentication auth, String accountNo) {
        String userEmail = auth.getName(); // email of logged-in user

        Account account = accountRepository.findByAccountNumber(accountNo);
        if (account == null) return false;

        System.out.println("User logged in: " + userEmail);
        System.out.println("Roles: " + auth.getAuthorities());

        return account.getUser().getEmail().equals(userEmail); // allow if the account belongs to the logged-in user
    }
}

