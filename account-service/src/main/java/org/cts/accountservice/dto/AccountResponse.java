package org.cts.accountservice.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cts.accountservice.enums.AccountRequestStatus;
import org.cts.accountservice.enums.AccountStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private String accountType;
    private Long branchId;
    private String branchName;

    // Core Account Fields
    private Double balance;       // This was missing!
    private String accountNumber;
    private AccountStatus status;        // Added to match your mapToResponse logic

    // Request-Specific Fields
    private Double initialDeposit;
    private String customerNote;
    private AccountRequestStatus requestStatus;
    private Long createdAccountId;
    private String createdAccountNumber;
    private String actionedBy;
    private String rejectionReason;
    private LocalDateTime actionedAt;
    private LocalDateTime createdAt;
    private LocalDateTime openedDate; // Added for mapping
    private String openedBy;          // Added for mapping
}