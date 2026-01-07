package com.zainab.PearsonBank.repository;

import com.zainab.PearsonBank.entity.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Boolean existsByEmail(String emailAddress);
    Optional<User> findByEmail(String emailAddress);
    Optional<User> findByResetPasswordToken(String token);

    @NotNull List<User> findAll();

    @Modifying
    @Query("""
        UPDATE User u SET u.appPassword = NULL, u.isDefaultPassword = false
        WHERE u.isDefaultPassword = true AND u.defaultPasswordIssuedAt < :expiryTime
        """)
    int invalidateExpiredDefaultPasswords(@Param("expiryTime") LocalDateTime expiryTime);

}
