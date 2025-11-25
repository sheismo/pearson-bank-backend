package com.zainab.PearsonBank.repository;

import com.zainab.PearsonBank.entity.UserSession;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<UserSession,UUID> {
    UserSession findByAccessToken(String accessToken);
    List<UserSession> findAllByUserId(UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSession s WHERE s.userId = :userId")
    void revokeAllSessionsByUserId(@Param("userId") UUID userId);
}
