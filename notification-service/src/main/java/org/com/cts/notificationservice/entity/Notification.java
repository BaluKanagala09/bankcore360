package org.com.cts.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.com.cts.notificationservice.enums.NotificationChannel;
import org.com.cts.notificationservice.enums.NotificationType;

//import java.nio.channels.Channel;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    @Enumerated(EnumType.STRING)
    private NotificationType type; // LOAN_APPROVED, KYC_REJECTED...

    private String title;
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel; // EMAIL, PUSH, BOTH

    private boolean readFlag = false;

    private LocalDateTime createdAt;
}