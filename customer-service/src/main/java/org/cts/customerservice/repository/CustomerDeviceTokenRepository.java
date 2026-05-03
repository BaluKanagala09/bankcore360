package org.cts.customerservice.repository;

import org.cts.customerservice.entity.CustomerDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerDeviceTokenRepository extends JpaRepository<CustomerDeviceToken,Long> {


    List<CustomerDeviceToken> findByCustomer_CustomerId(Long customerId);

    boolean existsByCustomer_CustomerIdAndDeviceToken(Long customerId, String deviceToken);



}
