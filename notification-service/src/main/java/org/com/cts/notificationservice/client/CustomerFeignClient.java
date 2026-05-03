package org.com.cts.notificationservice.client;

import org.com.cts.notificationservice.dto.CustomerNotificationProfile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service",
        url = "${customer.service.url}")
@Profile("!local")
public interface CustomerFeignClient {

    @GetMapping("/internal/customers/{customerId}/notification-profile")
    CustomerNotificationProfile getProfile(@PathVariable Long customerId);
}