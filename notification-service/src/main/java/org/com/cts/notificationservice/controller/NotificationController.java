package org.com.cts.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import org.com.cts.notificationservice.dto.NotificationResponse;
import org.com.cts.notificationservice.entity.Notification;
import org.com.cts.notificationservice.repository.NotificationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository repository;

    @GetMapping
    public List<NotificationResponse> getNotifications(
            @RequestParam Long customerId) {

        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(notification -> NotificationResponse.builder()
                        .id(notification.getId())
                        .type(notification.getType())
                        .title(notification.getTitle())
                        .message(notification.getMessage())
                        .read(notification.isReadFlag())
                        .createdAt(notification.getCreatedAt())
                        .build()
                ).toList();

    }
}
