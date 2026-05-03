package org.com.cts.notificationservice.dto;
import lombok.Builder;
import lombok.Getter;
import org.com.cts.notificationservice.enums.NotificationType;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private Long id;
    private NotificationType type;
}
