package com.zainab.PearsonBank.repository;


import com.zainab.PearsonBank.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByCustomerId(UUID customerId);

    boolean existsByCustomerId(UUID customerId);
    boolean existsByAccountNumber(String accountNumber);

    Account findByAccountNumber(String accountNumber);
}
