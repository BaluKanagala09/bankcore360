package org.com.cts.notificationservice.listener;

import lombok.RequiredArgsConstructor;
import org.com.cts.notificationservice.dto.NotificationEvent;
import org.com.cts.notificationservice.service.NotificationProcessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationProcessor processor;

    @KafkaListener(
            topics = "notification-events",
            groupId = "notification-service"
    )
    public void consume(NotificationEvent event) {

        processor.process(event);
    }

}
