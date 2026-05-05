package org.cts.transactionservice.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Account Service REST API responses.
 * Represents the full account information returned from the Account Service.
 * This is converted to AccountDto for internal use in Transaction Service.
 *
 * ⚠️ IMPORTANT: Account Service uses 'double' for balance, which has precision issues.
 * We use BigDecimal here with custom deserialization for financial accuracy.
 * Jackson automatically converts double → BigDecimal during deserialization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private String accountType;
    private String accountStatus;

    /**
     * Balance received from Account Service (sent as double, deserialized as BigDecimal).
     * Safe conversion: double → String → BigDecimal (preserves precision)
     *
     * Example:
     * Account Service: { "balance": 10000.50 }  (double)
     * Transaction Service: balance = BigDecimal("10000.50")  (precise)
     */
    @JsonDeserialize(using = DoubleToBigDecimalDeserializer.class)
    private BigDecimal balance;

    private String status;
    private Long customerId;
    private String customerName;
    private Long branchId;
    private String branchName;
    private LocalDateTime openedDate;
    private String openedBy;
}

