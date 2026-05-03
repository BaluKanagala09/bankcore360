package org.cts.customerservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cts.customerservice.auditlogs.Auditable;
import org.cts.customerservice.enums.KycStatus;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Core customer entity.
 * KYC must be APPROVED before an account can be opened or a loan applied.
 */
@Entity
@Table(name = "customers")
@SQLRestriction("is_deleted=false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends Auditable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    /**
     * KYC status — using an Enum is safer than Boolean for banking workflows.
     * Defaults to PENDING on registration.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 20)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.PENDING;

    /** Linked User account (for login) */
    @Column(name="user_id",nullable = false)
    private long userId;


    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    /** Bidirectional One-to-One mapping with CustomerInfo */
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
    private CustomerInfo info;

    /** One-to-Many: a customer can have CURRENT and PERMANENT addresses */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    // ---- Transient helpers ----

    @Transient
    public String getFullName() {
        if (info != null) {
            return info.getFirstName() + " " + info.getLastName();
        }
        return null;
    }

    @Transient
    public boolean isKycApproved() {
        return KycStatus.APPROVED.equals(this.kycStatus);
    }

    public void softDelete(String deletedBy) {
        LocalDateTime now = LocalDateTime.now();
        this.setIsDeleted(true);
        this.setDeletedAt(now);
        this.setDeletedBy(deletedBy);

        if (this.info != null) {
            this.info.setIsDeleted(true);
            this.info.setDeletedAt(now);
            this.info.setDeletedBy(deletedBy);
        }
        if (this.addresses != null) {
            this.addresses.forEach(
                    addr -> {
                        addr.setIsDeleted(true);
                        addr.setDeletedAt(now);
                        addr.setDeletedBy(deletedBy);
                    });
        }
    }

}