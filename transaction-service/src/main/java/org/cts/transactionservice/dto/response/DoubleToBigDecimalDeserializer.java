package org.cts.transactionservice.dto.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Custom Jackson deserializer that converts Account Service's 'double' balance
 * values to BigDecimal for financial accuracy.
 *
 * Why this is needed:
 * - Account Service sends balance as double (prone to floating-point precision errors)
 * - Transaction Service uses BigDecimal for financial calculations
 * - This deserializer ensures safe conversion: double → String → BigDecimal
 *
 * Example:
 * Account Service sends: { "balance": 10000.50 }  (as double)
 * Jackson deserializes to: BigDecimal("10000.50")  (precise)
 */
public class DoubleToBigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String value = jsonParser.getText();

        // Handle null values
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            // Convert via String to avoid floating-point precision issues
            // This ensures: 0.1 + 0.2 = 0.3 (not 0.30000000000000004)
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid balance value: " + value, e);
        }
    }
}
