package org.cts.transactionservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cts.transactionservice.dto.request.TransactionRequest;
import org.cts.transactionservice.dto.response.TransactionResponse;
import org.cts.transactionservice.enums.TransactionType;
import org.cts.transactionservice.security.AuthenticatedUser;
import org.cts.transactionservice.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 1. Perform a transaction (TRANSFER or SELF_TRANSFER).
     *    Only CUSTOMER role. Validates fromAccountId == logged-in user's accountId.
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TransactionResponse> performTransaction(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.performTransaction(request, user));
    }

    /**
     * 2. Get complete transaction history for the logged-in customer.
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(user));
    }

    /**
     * 3. Get all incoming (credit) transactions for the logged-in customer.
     */
    @GetMapping("/incoming")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<TransactionResponse>> getIncomingTransactions(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(transactionService.getIncomingTransactions(user));
    }

    /**
     * 4. Get all outgoing (debit) transactions for the logged-in customer.
     */
    @GetMapping("/outgoing")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<TransactionResponse>> getOutgoingTransactions(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(transactionService.getOutgoingTransactions(user));
    }

    /**
     * 5. Get transaction history within a date range for the logged-in customer.
     *    Query params: from, to (ISO 8601 date-time)
     *    Example: ?from=2025-01-01T00:00:00&to=2025-12-31T23:59:59
     */
    @GetMapping("/history/date-range")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<TransactionResponse>> getHistoryByDateRange(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(transactionService.getTransactionHistoryByDateRange(user, from, to));
    }

    /**
     * 6. Get transaction history within a date range for a specific branch.
     *    Only BRANCH_MANAGER role. Manager can only access their own branch.
     */
    @GetMapping("/branch/{branchId}/date-range")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    public ResponseEntity<List<TransactionResponse>> getBranchHistoryByDateRange(
            @PathVariable Long branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(transactionService.getBranchTransactionsByDateRange(branchId, from, to, user));
    }

    /**
     * 7. Get transaction history across all branches within a date range.
     *    Only ADMIN role.
     */
    @GetMapping("/all/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getAllHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(transactionService.getAllTransactionsByDateRange(from, to));
    }

    /**
     * 8. Retrieve all transactions for a specific branch.
     *    Only BRANCH_MANAGER role. Manager can only access their own branch.
     */
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    public ResponseEntity<List<TransactionResponse>> getBranchTransactions(
            @PathVariable Long branchId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(transactionService.getBranchTransactions(branchId, user));
    }

    /**
     * 9. Retrieve all transactions across all branches.
     *    Only ADMIN role.
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    /**
     * 10. Get transaction history by transaction type for the logged-in customer.
     *     Query param: type (TRANSFER or SELF_TRANSFER)
     */
    @GetMapping("/history/type")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<TransactionResponse>> getHistoryByType(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam TransactionType type) {
        return ResponseEntity.ok(transactionService.getTransactionHistoryByType(user, type));
    }

    /**
     * 11. Get incoming (credit) transactions for a branch.
     *     Only BRANCH_MANAGER role. Manager can only access their own branch.
     */
    @GetMapping("/branch/{branchId}/incoming")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    public ResponseEntity<List<TransactionResponse>> getBranchIncomingTransactions(
            @PathVariable Long branchId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(transactionService.getBranchIncomingTransactions(branchId, user));
    }

    /**
     * 12. Get outgoing (debit) transactions for a branch.
     *     Only BRANCH_MANAGER role. Manager can only access their own branch.
     */
    @GetMapping("/branch/{branchId}/outgoing")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    public ResponseEntity<List<TransactionResponse>> getBranchOutgoingTransactions(
            @PathVariable Long branchId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(transactionService.getBranchOutgoingTransactions(branchId, user));
    }
}
