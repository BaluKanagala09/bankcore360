package org.cts.transactionservice.stub;

import org.cts.transactionservice.dto.response.AccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// feign client communicating with account service
@FeignClient(
        name = "account-service",
        url = "${account.service.url:http://localhost:8081}"
)
public interface AccountServiceClient {

    // fetch acc details by acc_id
    @GetMapping("/api/v1/accounts/{accountId}")
    AccountDto getAccountById(@PathVariable("accountId") Long accountId);

    // check if an acc exists
    @GetMapping("/api/v1/accounts/{accountId}/exists")
    boolean accountExists(@PathVariable("accountId") Long accountId);

    @GetMapping("/api/v1/accounts/{accountId}/customer/{customerId}")
    boolean customerOwnsAccount(
            @PathVariable("customerId") Long customerId,
            @PathVariable("accountId") Long accountId
    );



}

