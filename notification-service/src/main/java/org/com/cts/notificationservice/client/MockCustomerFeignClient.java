package org.com.cts.notificationservice.client;

//package org.com.cts.notificationservice.client;

import org.com.cts.notificationservice.dto.CustomerNotificationProfile;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("local")
public class MockCustomerFeignClient implements CustomerFeignClient {

    @Override
    public CustomerNotificationProfile getProfile(Long customerId) {

        return CustomerNotificationProfile.builder()
                .customerId(customerId)
                .name("Test User")
                .email("eswarcharan2104@gmail.com")   // ✅ your real email
                .deviceTokens(List.of("mock-device-token"))
                .build();
    }
}
