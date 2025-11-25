package com.zainab.PearsonBank.repository;

import com.zainab.PearsonBank.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<UserSession,Long> {
    UserSession findByAccessToken(String accessToken);
}
