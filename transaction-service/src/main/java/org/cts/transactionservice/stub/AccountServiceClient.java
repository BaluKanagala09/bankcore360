package org.cts.transactionservice.stub;

import org.cts.transactionservice.dto.response.AccountDto;
import org.cts.transactionservice.enums.AccountType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stub simulating AccountService communication.
 * In production this would be a Feign client calling the account-service.
 *
 * Hardcoded accounts:
 * AccountId | CustomerId | BranchId | AccountType | Balance
 *    101    |     1      |    1     |   SAVINGS   | 50000
 *    102    |     1      |    2     |   CURRENT   | 30000
 *    103    |     2      |    1     |   SAVINGS   | 45000
 *    104    |     2      |    3     |   CURRENT   | 20000
 *    105    |     3      |    2     |   SAVINGS   | 60000
 *    106    |     3      |    3     |   CURRENT   | 25000
 */
@Service
public class AccountServiceClient {

    private static final Map<Long, AccountDto> ACCOUNTS = new HashMap<>();

    static {
        ACCOUNTS.put(101L, new AccountDto(101L, 1L, 1L, new BigDecimal("50000.00"), AccountType.SAVINGS));
        ACCOUNTS.put(102L, new AccountDto(102L, 1L, 2L, new BigDecimal("30000.00"), AccountType.CURRENT));
        ACCOUNTS.put(103L, new AccountDto(103L, 2L, 1L, new BigDecimal("45000.00"), AccountType.SAVINGS));
        ACCOUNTS.put(104L, new AccountDto(104L, 2L, 3L, new BigDecimal("20000.00"), AccountType.CURRENT));
        ACCOUNTS.put(105L, new AccountDto(105L, 3L, 2L, new BigDecimal("60000.00"), AccountType.SAVINGS));
        ACCOUNTS.put(106L, new AccountDto(106L, 3L, 3L, new BigDecimal("25000.00"), AccountType.CURRENT));
    }

    public Optional<AccountDto> getAccountById(Long accountId) {
        return Optional.ofNullable(ACCOUNTS.get(accountId));
    }

    public boolean accountExists(Long accountId) {
        return ACCOUNTS.containsKey(accountId);
    }

    public boolean customerOwnsAccount(Long customerId, Long accountId) {
        AccountDto account = ACCOUNTS.get(accountId);
        return account != null && account.getCustomerId().equals(customerId);
    }
}

