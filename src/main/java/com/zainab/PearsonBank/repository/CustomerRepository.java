package com.zainab.PearsonBank.repository;

import com.zainab.PearsonBank.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Boolean existsByEmail(String emailAddress);

    boolean existsByAccountNumber(String accountNumber);
}
