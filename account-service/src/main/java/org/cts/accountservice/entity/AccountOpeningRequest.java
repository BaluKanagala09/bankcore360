package org.cts.accountservice.entity;



import jakarta.persistence.*;
import lombok.*;
import org.cts.accountservice.enums.AccountRequestStatus;
import org.cts.accountservice.enums.AccountType;
import org.cts.accountservice.auditlogs.Auditable;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "account_opening_requests",
        indexes = {
                @Index(name = "idx_aor_customer_id", columnList = "customer_id"),
                @Index(name = "idx_aor_branch_id", columnList = "branch_id"),
                @Index(name = "idx_aor_status", columnList = "request_status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountOpeningRequest extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** External reference: Customer Service */
    @Column(name = "customer_id", nullable = false, updatable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20, updatable = false)
    private AccountType accountType;

    /** External reference: Branch Service */
    @Column(name = "branch_id", nullable = false, updatable = false)
    private Long branchId;

    @Column(name = "initial_deposit", nullable = false)
    @Builder.Default
    private Double initialDeposit = 0.0;

    /**
     * Account ID created after approval.
     * Null until request is APPROVED.
     */
    @Column(name = "created_account_id", updatable = false)
    private Long createdAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false, length = 20)
    @Builder.Default
    private AccountRequestStatus requestStatus = AccountRequestStatus.PENDING;

    /**
     * CSR identifier (email or employeeId)
     * who approved/rejected the request.
     */
    @Column(name = "actioned_by", length = 150)
    private String actionedBy;

    /**
     * Reason for rejection (only when REJECTED).
     */
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    /**
     * Timestamp when the request was actioned.
     */
    @Column(name = "actioned_at")
    private LocalDateTime actionedAt;
}