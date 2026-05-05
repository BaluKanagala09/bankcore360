package org.cts.transactionservice.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for balance update operations.
 * Returned by Account Service after a balance update request (DEBIT or CREDIT).
 *
 * ⚠️ IMPORTANT: All balance fields are deserialized from double to BigDecimal
 * for financial accuracy and precision.
 *
 * Example:
 * Account Service response:
 * {
 *   "previousBalance": 10000.50,
 *   "newBalance": 9500.50,
 *   "transactionAmount": 500.00
 * }
 *
 * Transaction Service receives:
 * previousBalance = BigDecimal("10000.50")  ✅ Precise
 * newBalance = BigDecimal("9500.50")        ✅ Precise
 * transactionAmount = BigDecimal("500.00")  ✅ Precise
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceUpdateResponse {
    private Long accountId;

    /**
     * Balance before the update operation (received as double, deserialized as BigDecimal)
     */
    @JsonDeserialize(using = DoubleToBigDecimalDeserializer.class)
    private BigDecimal previousBalance;

    /**
     * Balance after the update operation (received as double, deserialized as BigDecimal)
     */
    @JsonDeserialize(using = DoubleToBigDecimalDeserializer.class)
    private BigDecimal newBalance;

    /**
     * Amount that was debited/credited (received as double, deserialized as BigDecimal)
     */
    @JsonDeserialize(using = DoubleToBigDecimalDeserializer.class)
    private BigDecimal transactionAmount;

    private String type; // "DEBIT" or "CREDIT"
    private String message;
    private boolean success;
}


