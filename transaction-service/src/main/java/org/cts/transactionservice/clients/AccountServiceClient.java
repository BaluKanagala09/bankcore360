package org.cts.transactionservice.clients;

import org.cts.transactionservice.dto.request.BalanceUpdateRequest;
import org.cts.transactionservice.dto.response.AccountResponse;
import org.cts.transactionservice.dto.response.BalanceUpdateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// feign client communicating with account service
@FeignClient(
        name = "account-service",
        url = "${account.service.url:https://192.168.0.4/accounts}"
)
public interface AccountServiceClient {

    // fetch acc details by acc_id
    @GetMapping("/{accountId}")
    AccountResponse getAccountById(@PathVariable("accountId") Long accountId);

    // check if an acc exists
    @GetMapping("/{accountId}/exists")
    boolean accountExists(@PathVariable("accountId") Long accountId);

    @GetMapping("/api/v1/accounts/{accountId}/customer/{customerId}")
    boolean customerOwnsAccount(
            @PathVariable("customerId") Long customerId,
            @PathVariable("accountId") Long accountId
    );


    /**
     * Update account balance (debit or credit)
     * @param request BalanceUpdateRequest containing accountId, amount, and type (DEBIT/CREDIT)
     * @return BalanceUpdateResponse with updated balance information
     */
    @PostMapping("/accounts/balance/update")
    BalanceUpdateResponse updateBalance(@RequestBody BalanceUpdateRequest request);

}

