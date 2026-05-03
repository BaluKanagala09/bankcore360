package org.cts.customerservice.controller;

import lombok.RequiredArgsConstructor;
import org.cts.customerservice.dto.CustomerNotificationResponse;
import org.cts.customerservice.service.CustomerNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/customers")
@RequiredArgsConstructor
public class CustomerNotificationController {

    private final CustomerNotificationService service;

    @GetMapping("/{customerId}/notification-profile")
    public ResponseEntity<CustomerNotificationResponse>
    getNotificationProfile(@PathVariable Long customerId) {

        return ResponseEntity.ok(
                service.getNotificationProfile(customerId)
        );
    }
}