package org.cts.transactionservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating account balance.
 * Used when debiting or crediting an account after a transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceUpdateRequest {
    private Long accountId;
    private BigDecimal amount;
    private String type; // "DEBIT" or "CREDIT"
    private String reason; // e.g., "Transaction to account 2", "Transfer from account 1"
}

