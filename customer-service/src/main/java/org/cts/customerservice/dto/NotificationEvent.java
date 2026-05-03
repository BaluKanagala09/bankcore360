package org.cts.customerservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    private String eventType;
    private Long customerId;
    private Map<String, Object> data;
    private LocalDateTime timestamp;
}
