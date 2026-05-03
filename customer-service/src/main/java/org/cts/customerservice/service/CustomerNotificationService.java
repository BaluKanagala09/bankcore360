package org.cts.customerservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.cts.customerservice.dto.CustomerNotificationResponse;
import org.cts.customerservice.dto.DeviceTokenRequest;
import org.cts.customerservice.entity.Customer;
import org.cts.customerservice.entity.CustomerDeviceToken;
import org.cts.customerservice.repository.CustomerDeviceTokenRepository;
import org.cts.customerservice.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerNotificationService {

    private final CustomerRepository customerRepository;
    private final CustomerDeviceTokenRepository tokenRepository;

    @Transactional(readOnly = true)
    public CustomerNotificationResponse getNotificationProfile(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        List<String> tokens = tokenRepository
                .findByCustomer_CustomerId(customerId)
                .stream()
                .map(CustomerDeviceToken::getDeviceToken)
                .toList();

        return CustomerNotificationResponse.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getFullName())
                .email(customer.getInfo().getEmail())
                .deviceTokens(tokens)
                .build();
    }

    @Transactional
    public void registerDeviceToken(Long customerId, DeviceTokenRequest request) {

        if (tokenRepository.existsByCustomer_CustomerIdAndDeviceToken(
                customerId, request.getDeviceToken())) {
            return; // idempotent
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        CustomerDeviceToken token = CustomerDeviceToken.builder()
                .customer(customer)
                .deviceToken(request.getDeviceToken())
                .platform(request.getPlatform())
                .build();

        tokenRepository.save(token);
    }
}