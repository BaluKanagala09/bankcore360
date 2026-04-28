package org.cts.branchservice.entity;

import jakarta.persistence.*;
import lombok.*;
// CFD
//import org.cts.bankcore360.modules.user.entity.User;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name="branches",
        uniqueConstraints = {
                @UniqueConstraint(columnNames="branch_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_code", nullable = false, unique = true, length = 50)
    private String branchCode;

    @Column(name = "branch_name", nullable = false, length = 150)
    private String branchName;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    /**
     * ID of the Branch Manager (User).
     * Stored as plain ID to avoid circular module dependency.
     */
    @Column(name = "branch_manager_id")
    private Long branchManagerId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Staff users assigned to this branch */

    // CFD
//    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
//    @Builder.Default
//    private Set<User> users = new HashSet<>();
}
