package org.cts.accountservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cts.accountservice.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerServiceClient {

    private final WebClient.Builder webClientBuilder;
    private static final String CS_URL = "lb://customer-service";

    @CircuitBreaker(name = "customer-service", fallbackMethod = "kycCheckFallback")
    public boolean isKycApproved(Long customerId) {
        var response = webClientBuilder.build()
                .get()
                .uri(CS_URL + "/customers/internal/" + customerId + "/kyc-check")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) return false;
        return Boolean.TRUE.equals(response.get("approved"));
    }

    @CircuitBreaker(name = "customer-service", fallbackMethod = "getNameFallback")
    public String getCustomerName(Long customerId) {
        var response = webClientBuilder.build()
                .get()
                .uri(CS_URL + "/customers/internal/" + customerId + "/name")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) return "Unknown";
        return (String) response.getOrDefault("fullName", "Unknown");
    }

    public boolean kycCheckFallback(Long customerId, Throwable t) {
        throw new BusinessException("Customer service unavailable for KYC check.");
    }

    public String getNameFallback(Long customerId, Throwable t) {
        return "Unknown";
    }
}