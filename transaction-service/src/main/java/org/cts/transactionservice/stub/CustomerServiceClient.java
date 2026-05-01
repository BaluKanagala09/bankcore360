package org.cts.transactionservice.stub;

import org.cts.transactionservice.dto.response.CustomerDto;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stub simulating CustomerService communication.
 * In production this would be a Feign client calling the customer-service.
 */
@Service
public class CustomerServiceClient {

    private static final Map<Long, CustomerDto> CUSTOMERS = new HashMap<>();

    static {
        CUSTOMERS.put(1L, new CustomerDto(1L, "Alice Johnson", "alice@example.com"));
        CUSTOMERS.put(2L, new CustomerDto(2L, "Bob Smith", "bob@example.com"));
        CUSTOMERS.put(3L, new CustomerDto(3L, "Charlie Brown", "charlie@example.com"));
    }

    public Optional<CustomerDto> getCustomerById(Long customerId) {
        return Optional.ofNullable(CUSTOMERS.get(customerId));
    }

    public boolean customerExists(Long customerId) {
        return CUSTOMERS.containsKey(customerId);
    }
}

