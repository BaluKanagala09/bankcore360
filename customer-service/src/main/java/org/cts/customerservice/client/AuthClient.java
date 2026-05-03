package org.cts.customerservice.client;

import org.cts.customerservice.dto.UserResponse;
import org.cts.customerservice.utils.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "auth-service",
        path = "/auth/internal",
        fallback = AuthClientFallback.class
)
public interface AuthClient {

    @PostMapping("/create-user")
    ApiResponse<UserResponse> createCustomerUser(
            @RequestBody Map<String, String> payload
    );
}