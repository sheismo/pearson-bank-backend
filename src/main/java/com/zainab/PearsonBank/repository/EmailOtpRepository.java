package com.zainab.PearsonBank.repository;

import com.zainab.PearsonBank.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findTopByEmailAndOtpAndUsedFalseOrderByExpiryTimeDesc(String email, String otp);
    Optional<EmailOtp> findByEmail(String email);
    void deleteByEmail(String email);

}
