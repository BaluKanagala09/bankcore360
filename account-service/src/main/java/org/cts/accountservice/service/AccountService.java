package org.cts.accountservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cts.accountservice.client.CustomerServiceClient;
import org.cts.accountservice.enums.AccountStatus;
import org.cts.accountservice.enums.AccountType;
import org.cts.accountservice.exception.BusinessException;
import org.cts.accountservice.exception.ResourceNotFoundException;
import org.cts.accountservice.dto.*;
import org.cts.accountservice.entity.*;
import org.cts.accountservice.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerServiceClient customerServiceClient;

    @Transactional
    public AccountResponse openAccount(AccountRequest request, String openedBy) {

        boolean kycApproved = customerServiceClient.isKycApproved(request.getCustomerId());
        if (!kycApproved) {
            throw new BusinessException("KYC not approved.");
        }

        AccountType accountType;
        try {
            accountType = AccountType.valueOf(request.getAccountType().toUpperCase());
        } catch (Exception e) {
            throw new BusinessException("Invalid account type");
        }

        if (accountRepository.existsByCustomerIdAndAccountType(request.getCustomerId(), accountType)) {
            throw new BusinessException("Account already exists");
        }

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(accountType)
                .balance(request.getInitialDeposit())
                .status(AccountStatus.ACTIVE)
                .customerId(request.getCustomerId())
                .branchId(request.getBranchId())
                .openedDate(LocalDateTime.now())
                .openedBy(openedBy)
                .build();

        Account saved = accountRepository.save(account);

        return toResponse(saved,
                customerServiceClient.getCustomerName(saved.getCustomerId()));
    }

    //fix this

    public AccountResponse findById(Long id) {
        Account account =accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        return toResponse(account, "eswar");
    }

    private String generateAccountNumber() {
        return String.valueOf(100000000000L +
                Math.abs(new Random().nextLong() % 900000000000L));
    }

    private AccountResponse toResponse(Account a, String name) {
        return AccountResponse.builder()
                .id(a.getId())
                .accountNumber(a.getAccountNumber())
                .accountType(a.getAccountType().name())
                .balance(a.getBalance())
                .status(AccountStatus.valueOf(a.getStatus().name()))
                .customerId(a.getCustomerId())
                .customerName(name)
                .build();
    }
}