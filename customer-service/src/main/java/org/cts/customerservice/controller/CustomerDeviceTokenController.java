package org.cts.customerservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cts.customerservice.dto.DeviceTokenRequest;
import org.cts.customerservice.service.CustomerNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerDeviceTokenController {

    private final CustomerNotificationService service;

    @PostMapping("/{customerId}/device-token")
    public ResponseEntity<Void> registerDeviceToken(
            @PathVariable Long customerId,
            @RequestBody @Valid DeviceTokenRequest request) {

        service.registerDeviceToken(customerId, request);
        return ResponseEntity.ok().build();
    }
}
