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

    // All transactions for a customer (sent or received)
    @Query("SELECT t FROM Transaction t WHERE t.fromCustomerId = :customerId OR t.toCustomerId = :customerId")
    List<Transaction> findAllByCustomerId(@Param("customerId") Long customerId);

    // Incoming (credit) transactions for a customer
    List<Transaction> findByToCustomerId(Long toCustomerId);

    // Outgoing (debit) transactions for a customer
    List<Transaction> findByFromCustomerId(Long fromCustomerId);

    // By date range for a specific customer
    @Query("SELECT t FROM Transaction t WHERE (t.fromCustomerId = :customerId OR t.toCustomerId = :customerId) AND t.createdAt BETWEEN :from AND :to")
    List<Transaction> findByCustomerIdAndDateRange(
            @Param("customerId") Long customerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

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

    // By transaction type for a customer
    @Query("SELECT t FROM Transaction t WHERE (t.fromCustomerId = :customerId OR t.toCustomerId = :customerId) AND t.transactionType = :type")
    List<Transaction> findByCustomerIdAndType(
            @Param("customerId") Long customerId,
            @Param("type") TransactionType type);
}
