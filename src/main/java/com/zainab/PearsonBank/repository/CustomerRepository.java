package com.zainab.PearsonBank.repository;

import com.zainab.PearsonBank.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Boolean existsByEmail(String emailAddress);

}
