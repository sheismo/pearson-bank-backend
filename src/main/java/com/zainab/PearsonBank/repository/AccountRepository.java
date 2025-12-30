package com.zainab.PearsonBank.repository;


import com.zainab.PearsonBank.entity.Account;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByUserId(UUID customerId);

    boolean existsByUserId(UUID customerId);
    boolean existsByAccountNumber(String accountNumber);

    Account findByAccountNumber(String accountNumber);
    @NotNull List<Account> findAll();
}
