package org.com.cts.notificationservice;

import org.com.cts.notificationservice.dto.NotificationEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.LocalDateTime;
import java.util.Map;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = { "notification-events" },
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092",
                "port=9092"
        }
)
class NotificationKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Test
    void testKafkaNotificationFlow() throws Exception {

        NotificationEvent event = NotificationEvent.builder()
                .eventType("LOAN_APPROVED")
                .customerId(1L)
                .data(Map.of(
                        "loanAmount", 500000,
                        "emi", 12000,
                        "dueDate", "2026-05-05"
                ))
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("notification-events", event);

        // give consumer time
        Thread.sleep(2000);

        // ✅ verify via DB, logs, or mocks
    }
}
