package com.zainab.PearsonBank.repository;

import com.zainab.PearsonBank.entity.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Boolean existsByEmail(String emailAddress);
    Optional<User> findByEmail(String emailAddress);
    Optional<User> findByResetPasswordToken(String token);

    @NotNull List<User> findAll();
}
