package org.cts.transactionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cts.transactionservice.enums.TransactionStatus;
import org.cts.transactionservice.enums.TransactionType;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String referenceNumber;

    @Column(nullable = false)
    private Long fromAccountId;

    @Column(nullable = false)
    private Long toAccountId;

    @Column(nullable = false)
    private Long fromCustomerId;

    @Column(nullable = false)
    private Long toCustomerId;

    /** Branch of the sender's account */
    @Column(nullable = false)
    private Long fromBranchId;

    /** Branch of the receiver's account */
    @Column(nullable = false)
    private Long toBranchId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    private String description;

    private String failureReason;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime transactionDate;
}

