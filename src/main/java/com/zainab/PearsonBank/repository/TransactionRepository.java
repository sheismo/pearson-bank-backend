package com.zainab.PearsonBank.repository;

import com.zainab.PearsonBank.entity.Transaction;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Transaction findByTransactionId(UUID transactionId);
    List<Transaction> findAllByInitiator(UUID customerId);

//    @Query("SELECT t from Transaction t WHERE t.initiator = :customerId AND t.createdDate between :startDate and :endDate")
    List<Transaction> findAllByInitiatorAndCreatedDateBetween(UUID customerId, LocalDateTime startDate, LocalDateTime endDate);

    @NotNull List<Transaction> findAll();

}
