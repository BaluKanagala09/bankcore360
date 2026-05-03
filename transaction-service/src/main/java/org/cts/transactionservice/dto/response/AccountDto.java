package org.cts.transactionservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cts.transactionservice.enums.AccountStatus;
import org.cts.transactionservice.enums.AccountType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Long accountId;
    private Long customerId;
    private Long branchId;
    private BigDecimal balance;
//    private String accountType;
    private String accountStatus;

    // ── New fields for Loan Service ──────────────────────────
    private Integer salaryCreditCountLast6Months;
    private Integer salaryMissedMonths;
    private Integer recentOverdraftCount;
}

