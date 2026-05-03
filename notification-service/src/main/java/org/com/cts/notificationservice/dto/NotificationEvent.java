package org.com.cts.notificationservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    private String eventType; // LOAN_APPROVED, KYC_REJECTED, etc.

    private Long customerId;

    private Map<String, Object> data;

    private LocalDateTime timestamp;

    // Helper methods

    public boolean isTransactionEvent() {
        return "TRANSACTION_SUCCESS".equals(eventType)
                || "TRANSACTION_FAILED".equals(eventType);
    }
}

