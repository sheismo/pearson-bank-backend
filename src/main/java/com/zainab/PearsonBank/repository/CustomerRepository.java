package com.zainab.PearsonBank.repository;

import com.zainab.PearsonBank.entity.Customer;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Boolean existsByEmail(String emailAddress);
    Optional<Customer> findByEmail(String emailAddress);
    Optional<Customer> findByResetPasswordToken(String token);

    @NotNull List<Customer> findAll();
}
