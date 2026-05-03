package org.cts.transactionservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cts.transactionservice.dto.request.TransactionRequest;
import org.cts.transactionservice.dto.response.AccountDto;
import org.cts.transactionservice.dto.response.TransactionResponse;
import org.cts.transactionservice.entity.Transaction;
import org.cts.transactionservice.enums.TransactionStatus;
import org.cts.transactionservice.enums.TransactionType;
import org.cts.transactionservice.dto.request.BalanceUpdateRequest;
import org.cts.transactionservice.exception.AccountOwnershipException;
import org.cts.transactionservice.exception.BranchAccessDeniedException;
import org.cts.transactionservice.exception.BranchNotFoundException;
import org.cts.transactionservice.exception.InvalidTransactionException;
import org.cts.transactionservice.mapper.AccountMapper;
import org.cts.transactionservice.repository.TransactionRepository;
import org.cts.transactionservice.security.AuthenticatedUser;
import org.cts.transactionservice.clients.AccountServiceClient;
import org.cts.transactionservice.clients.BranchServiceClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final BranchServiceClient branchServiceClient;
    private final AccountMapper accountMapper;

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

        // Fetch and convert AccountResponse from Account Service to AccountDto
        AccountDto fromAccount = accountMapper.toAccountDto(accountServiceClient.getAccountById(request.getFromAccountId()));
//                .orElseThrow(() -> new AccountNotFoundException("Source account not found: " + request.getFromAccountId()));

        AccountDto toAccount = accountMapper.toAccountDto(accountServiceClient.getAccountById(request.getToAccountId()));
//                .orElseThrow(() -> new AccountNotFoundException("Destination account not found: " + request.getToAccountId()));

        if (!branchServiceClient.branchExists(fromAccount.getBranchId())) {
            throw new BranchNotFoundException("Branch not found: " + fromAccount.getBranchId());
        }

        if (request.getTransactionType() == TransactionType.TRANSFER) {
            validateTransfer(fromAccount, toAccount);
        } else if (request.getTransactionType() == TransactionType.SELF_TRANSFER) {
            validateSelfTransfer(fromAccount, toAccount, request);
        }

        String failureReason = validateTransactionCanBeProcessed(fromAccount,request);

        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber())
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .fromCustomerId(fromAccount.getCustomerId())
                .toCustomerId(toAccount.getCustomerId())
                .fromBranchId(fromAccount.getBranchId())
                .toBranchId(toAccount.getBranchId())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .description(request.getDescription())
                .build();

        if(failureReason == null){
            transaction.setStatus(TransactionStatus.SUCCESS);

            // ── Update account balances after successful transaction ──────────
            try {
                // Debit the source account
                accountServiceClient.updateBalance(
                    BalanceUpdateRequest.builder()
                        .accountId(request.getFromAccountId())
                        .amount(request.getAmount())
                        .type("DEBIT")
                        .reason("Transaction to account " + request.getToAccountId())
                        .build()
                );
                log.info("Debited account {} with amount {}", request.getFromAccountId(), request.getAmount());

                // Credit the destination account
                accountServiceClient.updateBalance(
                    BalanceUpdateRequest.builder()
                        .accountId(request.getToAccountId())
                        .amount(request.getAmount())
                        .type("CREDIT")
                        .reason("Transaction from account " + request.getFromAccountId())
                        .build()
                );
                log.info("Credited account {} with amount {}", request.getToAccountId(), request.getAmount());

            } catch (Exception e) {
                // If balance update fails, mark transaction as failed but still save it for audit
                log.error("Failed to update account balances for transaction: {}", e.getMessage());
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailureReason("Balance update failed: " + e.getMessage());
            }

            log.info("Transaction {} completed with status: {}", transaction.getId(), transaction.getStatus());
        }
        else{
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(failureReason);
            log.warn("Transaction failed: {}", failureReason);
        }

        transaction = transactionRepository.save(transaction);
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

    // ---- Validates if a transaction can be processed based on business rules (e.g. sufficient balance, daily limits) ----
    private String validateTransactionCanBeProcessed(AccountDto fromAccount, TransactionRequest request){

        // 1. if from account is blocked
        if("BLOCKED".equals(fromAccount.getAccountStatus()) || "CLOSED".equals(fromAccount.getAccountStatus())){
            return "Account is blocked and cannot send transactions";
        }

        // 2. Check insufficient balance
        if(fromAccount.getBalance() != null && fromAccount.getBalance().compareTo(request.getAmount()) < 0){
            return "Insufficient balance. Available: " + fromAccount.getBalance() + ", Required: " + request.getAmount();
        }

        // 3. check daily limit

        return null;
    }

    // ── 2. Customer: all transaction history (account-specific) ────────────────

    @Override
    public List<TransactionResponse> getTransactionHistory(AuthenticatedUser user) {
        return transactionRepository.findAllByAccountId(user.getAccountId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── 3. Customer: incoming (credit) (account-specific) ──────────────────────

    @Override
    public List<TransactionResponse> getIncomingTransactions(AuthenticatedUser user) {
        return transactionRepository.findByToAccountId(user.getAccountId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── 4. Customer: outgoing (debit) (account-specific) ───────────────────────

    @Override
    public List<TransactionResponse> getOutgoingTransactions(AuthenticatedUser user) {
        return transactionRepository.findByFromAccountId(user.getAccountId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── 5. Customer: date range (account-specific) ────────────────────────────

    @Override
    public List<TransactionResponse> getTransactionHistoryByDateRange(
            AuthenticatedUser user, LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findByAccountIdAndCreatedAtBetween(user.getAccountId(), from, to)
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

    // ── 10. Customer: by transaction type (account-specific) ──────────────────

    @Override
    public List<TransactionResponse> getTransactionHistoryByType(AuthenticatedUser user, TransactionType type) {
        return transactionRepository.findByAccountIdAndType(user.getAccountId(), type)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Generates a unique reference number in format: TXN + yyyyMMddHHmmssSSS + 6-digit random number
     * Example: TXN202505011205301234567890
     */
    private String generateReferenceNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String timestamp = LocalDateTime.now().format(formatter);
        int randomNumber = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "TXN" + timestamp + randomNumber;
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
                .failureReason(t.getFailureReason())
                .createdAt(t.getCreatedAt())
                .build();
    }
}

