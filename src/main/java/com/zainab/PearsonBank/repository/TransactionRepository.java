package com.zainab.PearsonBank.repository;

import com.zainab.PearsonBank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Transaction findByTransactionId(UUID transactionId);
    List<Transaction> findAllByInitiator( UUID customerId);

    @Query("SELECT t from Transaction t WHERE t.initiator = :customerId AND t.createdDate between :startDate and :endDate")
    List<Transaction> findAllByInitiatorWithinRange(@Param("customerId") UUID customerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}
