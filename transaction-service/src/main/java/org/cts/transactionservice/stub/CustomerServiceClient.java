package org.cts.transactionservice.stub;

import org.cts.transactionservice.dto.response.CustomerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** * Feign client for communicating with Customer Service. * Calls the customer-service microservice. */
@FeignClient(
        name = "customer-service",
        url = "${customer-service.url:http://localhost:9083}"  // Configure in application.yaml
)
public interface CustomerServiceClient {

    /**     * Fetch customer details by customer ID     */
    @GetMapping("/api/v1/customers/{customerId}")
    CustomerDto getCustomerById(@PathVariable("customerId") Long customerId);

    /**     * Check if a customer exists     */
    @GetMapping("/api/v1/customers/{customerId}/exists")
    boolean customerExists(@PathVariable("customerId") Long customerId);
}