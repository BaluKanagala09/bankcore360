package org.cts.customerservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * WebClient configuration for inter-service communication.
 *
 * WHY @LoadBalanced?
 *   @LoadBalanced enables Eureka-based service discovery for WebClient.
 *   Without it, "http://customer-service" would fail (no DNS entry).
 *   With it, Spring Cloud LoadBalancer resolves "customer-service"
 *   to an actual IP:port from Eureka, and distributes requests across
 *   all registered instances (LOAD BALANCING).
 *
 * HOW JWT IS PROPAGATED:
 *   The API Gateway validates the JWT and injects X-User-Email
 *   and X-User-Role as headers before forwarding the request.
 *   When service A calls service B via WebClient, it passes
 *   those headers along — so service B knows who the caller is.
 *   No service re-validates the JWT (that's the gateway's job).
 */
@Slf4j
@Configuration
public class WebClientConfig {

    /**
     * Load-balanced WebClient builder.
     * Use this to build WebClient instances that can call other microservices by name.
     *
     * Usage in a service:
     *   webClientBuilder.build()
     *       .get()
     *       .uri("http://customer-service/customers/{id}", customerId)
     *       .retrieve()
     *       .bodyToMono(CustomerDto.class)
     *       .block();
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter(logRequest())
                .filter(logResponse());
    }

    /** Logs outgoing requests for debugging inter-service calls */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            log.debug("WebClient → {} {}", req.method(), req.url());
            return Mono.just(req);
        });
    }

    /** Logs incoming responses for debugging */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(res -> {
            log.debug("WebClient ← Status: {}", res.statusCode());
            return Mono.just(res);
        });
    }
}