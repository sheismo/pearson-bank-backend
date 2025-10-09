package com.zainab.PearsonBank.repository;

import com.zainab.PearsonBank.entity.FailedEmail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedEmailRepository extends JpaRepository<FailedEmail, String> {
}
