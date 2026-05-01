package org.cts.transactionservice.service;

import org.cts.transactionservice.dto.request.TransactionRequest;
import org.cts.transactionservice.dto.response.TransactionResponse;
import org.cts.transactionservice.enums.TransactionType;
import org.cts.transactionservice.security.AuthenticatedUser;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    TransactionResponse performTransaction(TransactionRequest request, AuthenticatedUser user);

    List<TransactionResponse> getTransactionHistory(AuthenticatedUser user);

    List<TransactionResponse> getIncomingTransactions(AuthenticatedUser user);

    List<TransactionResponse> getOutgoingTransactions(AuthenticatedUser user);

    List<TransactionResponse> getTransactionHistoryByDateRange(AuthenticatedUser user, LocalDateTime from, LocalDateTime to);


    List<TransactionResponse> getBranchTransactionsByDateRange(Long branchId, LocalDateTime from, LocalDateTime to, AuthenticatedUser user);

    List<TransactionResponse> getAllTransactionsByDateRange(LocalDateTime from, LocalDateTime to);

    List<TransactionResponse> getBranchTransactions(Long branchId, AuthenticatedUser user);

    List<TransactionResponse> getBranchIncomingTransactions(Long branchId, AuthenticatedUser user);

    List<TransactionResponse> getBranchOutgoingTransactions(Long branchId, AuthenticatedUser user);


    List<TransactionResponse> getAllTransactions();

    List<TransactionResponse> getTransactionHistoryByType(AuthenticatedUser user, TransactionType type);
}

