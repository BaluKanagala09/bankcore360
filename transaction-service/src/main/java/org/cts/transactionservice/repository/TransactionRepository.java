package org.cts.transactionservice.repository;

import org.cts.transactionservice.entity.Transaction;
import org.cts.transactionservice.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ── Account-specific queries ────────────────────────────────────────
    
    // All transactions for a specific account (sent or received)
    @Query("SELECT t FROM Transaction t WHERE t.fromAccountId = :accountId OR t.toAccountId = :accountId")
    List<Transaction> findAllByAccountId(@Param("accountId") Long accountId);

    // Incoming (credit) transactions for a specific account
    List<Transaction> findByToAccountId(Long toAccountId);

    // Outgoing (debit) transactions for a specific account
    List<Transaction> findByFromAccountId(Long fromAccountId);

    // Account transactions within a date range (sent or received)
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) AND t.createdAt BETWEEN :from AND :to")
    List<Transaction> findByAccountIdAndCreatedAtBetween(
            @Param("accountId") Long accountId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // By transaction type for a specific account
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) AND t.transactionType = :type")
    List<Transaction> findByAccountIdAndType(
            @Param("accountId") Long accountId,
            @Param("type") TransactionType type);


    // ── Branch-specific queries ──────────────────────────────────────────────
    
    // All transactions involving a branch (sent OR received by that branch)
    @Query("SELECT t FROM Transaction t WHERE t.fromBranchId = :branchId OR t.toBranchId = :branchId")
    List<Transaction> findAllByBranchId(@Param("branchId") Long branchId);

    // Outgoing (debit) transactions for a branch — sent FROM this branch
    List<Transaction> findByFromBranchId(Long fromBranchId);

    // Incoming (credit) transactions for a branch — received INTO this branch
    List<Transaction> findByToBranchId(Long toBranchId);

    // Branch transactions within a date range (sent or received)
    @Query("SELECT t FROM Transaction t WHERE (t.fromBranchId = :branchId OR t.toBranchId = :branchId) AND t.createdAt BETWEEN :from AND :to")
    List<Transaction> findByBranchIdAndCreatedAtBetween(
            @Param("branchId") Long branchId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // All transactions within a date range
    List<Transaction> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
