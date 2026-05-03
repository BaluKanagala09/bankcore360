package org.cts.customerservice.config;

import lombok.RequiredArgsConstructor;
import org.cts.customerservice.dto.NotificationEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventProducer {
    private final org.springframework.kafka.core.KafkaTemplate<String,
            org.cts.customerservice.dto.NotificationEvent> kafkaTemplate;

    private  static  final String TOPIC="notification-events";

    public void publish(NotificationEvent event){
        kafkaTemplate.send(TOPIC,event.getCustomerId().toString(),event);
    }
}
