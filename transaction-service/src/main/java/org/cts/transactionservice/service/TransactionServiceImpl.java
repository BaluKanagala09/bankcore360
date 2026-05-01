package org.cts.transactionservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cts.transactionservice.dto.request.TransactionRequest;
import org.cts.transactionservice.dto.response.AccountDto;
import org.cts.transactionservice.dto.response.TransactionResponse;
import org.cts.transactionservice.entity.Transaction;
import org.cts.transactionservice.enums.TransactionStatus;
import org.cts.transactionservice.enums.TransactionType;
import org.cts.transactionservice.exception.AccountNotFoundException;
import org.cts.transactionservice.exception.AccountOwnershipException;
import org.cts.transactionservice.exception.BranchAccessDeniedException;
import org.cts.transactionservice.exception.BranchNotFoundException;
import org.cts.transactionservice.exception.InvalidTransactionException;
import org.cts.transactionservice.repository.TransactionRepository;
import org.cts.transactionservice.security.AuthenticatedUser;
import org.cts.transactionservice.stub.AccountServiceClient;
import org.cts.transactionservice.stub.BranchServiceClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final BranchServiceClient branchServiceClient;

    // ── 1. Perform Transaction ────────────────────────────────────────────────

    @Override
    @Transactional
    public TransactionResponse performTransaction(TransactionRequest request, AuthenticatedUser user) {
        log.info("Performing {} transaction: account {} -> account {}",
                request.getTransactionType(), request.getFromAccountId(), request.getToAccountId());

        if (!request.getFromAccountId().equals(user.getAccountId())) {
            throw new AccountOwnershipException(
                    "You can only perform transactions from your own account (accountId=" + user.getAccountId() + ")");
        }

        AccountDto fromAccount = accountServiceClient.getAccountById(request.getFromAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Source account not found: " + request.getFromAccountId()));

        AccountDto toAccount = accountServiceClient.getAccountById(request.getToAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Destination account not found: " + request.getToAccountId()));

        if (!branchServiceClient.branchExists(fromAccount.getBranchId())) {
            throw new BranchNotFoundException("Branch not found: " + fromAccount.getBranchId());
        }

        if (request.getTransactionType() == TransactionType.TRANSFER) {
            validateTransfer(fromAccount, toAccount);
        } else if (request.getTransactionType() == TransactionType.SELF_TRANSFER) {
            validateSelfTransfer(fromAccount, toAccount, request);
        }

        Transaction transaction = Transaction.builder()
                .referenceNumber(UUID.randomUUID().toString())
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .fromCustomerId(fromAccount.getCustomerId())
                .toCustomerId(toAccount.getCustomerId())
                .fromBranchId(fromAccount.getBranchId())
                .toBranchId(toAccount.getBranchId())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Transaction {} completed successfully", transaction.getId());
        return toResponse(transaction);
    }

    // ── Business rule validators ──────────────────────────────────────────────

    private void validateTransfer(AccountDto fromAccount, AccountDto toAccount) {
        if (fromAccount.getCustomerId().equals(toAccount.getCustomerId())) {
            throw new InvalidTransactionException(
                    "TRANSFER must be between different customers. Use SELF_TRANSFER for same customer.");
        }
    }

    private void validateSelfTransfer(AccountDto fromAccount, AccountDto toAccount, TransactionRequest request) {
        if (!fromAccount.getCustomerId().equals(toAccount.getCustomerId())) {
            throw new InvalidTransactionException("SELF_TRANSFER must be between accounts of the same customer.");
        }
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new InvalidTransactionException("SELF_TRANSFER requires different source and destination accounts.");
        }
    }

    // ── Branch ownership guard ────────────────────────────────────────────────

    /**
     * Ensures the logged-in branch manager can only access their own branch.
     * Throws BranchAccessDeniedException if the requested branchId != user's branchId.
     */
    private void assertBranchAccess(Long requestedBranchId, AuthenticatedUser user) {
        if (!branchServiceClient.branchExists(requestedBranchId)) {
            throw new BranchNotFoundException("Branch not found: " + requestedBranchId);
        }
        if (!requestedBranchId.equals(user.getBranchId())) {
            throw new BranchAccessDeniedException(
                    "Access denied: you manage branch " + user.getBranchId() +
                    " and cannot access transactions of branch " + requestedBranchId);
        }
    }

    // ── 2. Customer: all transaction history ─────────────────────────────────

    @Override
    public List<TransactionResponse> getTransactionHistory(AuthenticatedUser user) {
        Long customerId = resolveCustomerId(user.getAccountId());
        return transactionRepository.findAllByCustomerId(customerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── 3. Customer: incoming (credit) ───────────────────────────────────────

    @Override
    public List<TransactionResponse> getIncomingTransactions(AuthenticatedUser user) {
        Long customerId = resolveCustomerId(user.getAccountId());
        return transactionRepository.findByToCustomerId(customerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── 4. Customer: outgoing (debit) ────────────────────────────────────────

    @Override
    public List<TransactionResponse> getOutgoingTransactions(AuthenticatedUser user) {
        Long customerId = resolveCustomerId(user.getAccountId());
        return transactionRepository.findByFromCustomerId(customerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── 5. Customer: date range ───────────────────────────────────────────────

    @Override
    public List<TransactionResponse> getTransactionHistoryByDateRange(
            AuthenticatedUser user, LocalDateTime from, LocalDateTime to) {
        Long customerId = resolveCustomerId(user.getAccountId());
        return transactionRepository.findByCustomerIdAndDateRange(customerId, from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── 6. Branch manager: date range (own branch only) ──────────────────────

    @Override
    public List<TransactionResponse> getBranchTransactionsByDateRange(
            Long branchId, LocalDateTime from, LocalDateTime to, AuthenticatedUser user) {
        assertBranchAccess(branchId, user);
        return transactionRepository.findByBranchIdAndCreatedAtBetween(branchId, from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── 7. Admin: all branches date range ────────────────────────────────────

    @Override
    public List<TransactionResponse> getAllTransactionsByDateRange(LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findByCreatedAtBetween(from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── 8. Branch manager: all transactions (own branch only) ────────────────

    @Override
    public List<TransactionResponse> getBranchTransactions(Long branchId, AuthenticatedUser user) {
        assertBranchAccess(branchId, user);
        return transactionRepository.findAllByBranchId(branchId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── 8a. Branch manager: incoming transactions (own branch only) ──────────

    @Override
    public List<TransactionResponse> getBranchIncomingTransactions(Long branchId, AuthenticatedUser user) {
        assertBranchAccess(branchId, user);
        return transactionRepository.findByToBranchId(branchId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── 8b. Branch manager: outgoing transactions (own branch only) ──────────

    @Override
    public List<TransactionResponse> getBranchOutgoingTransactions(Long branchId, AuthenticatedUser user) {
        assertBranchAccess(branchId, user);
        return transactionRepository.findByFromBranchId(branchId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── 9. Admin: all transactions ───────────────────────────────────────────

    @Override
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── 10. Customer: by transaction type ────────────────────────────────────

    @Override
    public List<TransactionResponse> getTransactionHistoryByType(AuthenticatedUser user, TransactionType type) {
        Long customerId = resolveCustomerId(user.getAccountId());
        return transactionRepository.findByCustomerIdAndType(customerId, type)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long resolveCustomerId(Long accountId) {
        return accountServiceClient.getAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId))
                .getCustomerId();
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .referenceNumber(t.getReferenceNumber())
                .fromAccountId(t.getFromAccountId())
                .toAccountId(t.getToAccountId())
                .fromCustomerId(t.getFromCustomerId())
                .toCustomerId(t.getToCustomerId())
                .fromBranchId(t.getFromBranchId())
                .toBranchId(t.getToBranchId())
                .amount(t.getAmount())
                .transactionType(t.getTransactionType())
                .status(t.getStatus())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .build();
    }
}

