package org.cts.accountservice.repository;


import org.cts.accountservice.entity.Account;
import org.cts.accountservice.enums.AccountStatus;
import org.cts.accountservice.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {

    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByCustomerId(Long customerId);
    List<Account> findByBranchId(Long branchId);
    List<Account> findByStatus(AccountStatus status);
    boolean existsByAccountNumber(String AccountNumber);
    boolean existsByCustomerIdAndAccountType(Long customerId, AccountType accountType);
}
