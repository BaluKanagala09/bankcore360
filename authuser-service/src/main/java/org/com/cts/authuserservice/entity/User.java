package org.com.cts.authuserservice.entity;
//package org.cts.bankcore360.modules.models;

import jakarta.persistence.*;
import lombok.*;
import org.cts.bankcore360.modules.auditlogs.Auditable;
import org.cts.bankcore360.modules.branch.entity.Branch;
import org.cts.bankcore360.modules.user.enums.UserRole;
import org.cts.bankcore360.modules.user.enums.UserStatus;

/**
 * Represents a system user (bank staff or customer).
 * Roles: ADMIN, BRANCH_MANAGER, CSR, LOAN_MANAGER, COMPLIANCE_OFFICER, CUSTOMER
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique login email */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** BCrypt-hashed password */
    @Column(nullable = false)
    private String password;

    /** Role determines access level */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    /**
     * Branch association — nullable for ADMIN and COMPLIANCE_OFFICER
     * who operate system-wide.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id",updatable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;
}
