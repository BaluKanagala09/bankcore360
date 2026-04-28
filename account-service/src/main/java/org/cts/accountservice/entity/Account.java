package org.cts.accountservice.entity;


import jakarta.persistence.*;
import lombok.*;
import org.cts.accountservice.enums.AccountStatus;
import org.cts.accountservice.enums.AccountType;
// CFD
//import org.cts.bankcore360.modules.account.enums.AccountStatus;
//import org.cts.bankcore360.modules.account.enums.AccountType;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_accounts_account_number", columnNames = "account_number")
        },
        indexes = {
                @Index(name = "idx_accounts_customer_id", columnList = "customer_id"),
                @Index(name = "idx_accounts_branch_id", columnList = "branch_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique 12-digit account number */
    @Column(name = "account_number", nullable = false, length = 12, updatable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(nullable = false)
    @Builder.Default
    private Double balance = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    /** External reference: Customer Service */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** External reference: Branch Service */
    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "opened_date", nullable = false, updatable = false)
    private LocalDateTime openedDate;

    /** CSR identifier (email or employeeId) */
    @Column(name = "opened_by", length = 150)
    private String openedBy;
}