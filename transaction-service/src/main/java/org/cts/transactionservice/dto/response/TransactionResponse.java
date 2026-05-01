package org.cts.transactionservice.dto.response;

import lombok.Builder;
import lombok.Data;
import org.cts.transactionservice.enums.TransactionStatus;
import org.cts.transactionservice.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private String referenceNumber;
    private Long fromAccountId;
    private Long toAccountId;
    private Long fromCustomerId;
    private Long toCustomerId;
    private Long fromBranchId;
    private Long toBranchId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String failureReason;
    private String description;
    private LocalDateTime createdAt;
}

