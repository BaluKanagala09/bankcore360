package org.cts.customerservice.client;

import org.cts.customerservice.dto.UserResponse;
import org.cts.customerservice.utils.ApiResponse;

import java.util.Map;

public class AuthClientFallback implements  AuthClient{

    @Override
    public ApiResponse<UserResponse> createCustomerUser(Map<String, String> payload) {
        throw new RuntimeException(
                "Auth service is unavailable. Please try again later."
        );
    }

}
