package org.cts.customerservice.repository;

import org.cts.customerservice.entity.Customer;
import org.cts.customerservice.enums.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {

    Optional<Customer> findByUserId(Long userId);
    List<Customer> findByKycStatus(KycStatus kycStatus);
    boolean existsByUserId(Long userId);

//    Optional<CustomerInfo> findByCustomerCustomerId(Long customerId);


    @Query("SELECT COUNT(ci) > 0 FROM CustomerInfo ci WHERE ci.aadhar = :aadhar")
    boolean existsByInfoAadhar(@Param("aadhar") String aadhar);

    /**
     * Checks if the given PAN already exists in customer_details.
     */
    @Query("SELECT COUNT(ci) > 0 FROM CustomerInfo ci WHERE ci.pan = :pan")
    boolean existsByInfoPan(@Param("pan") String pan);

    // ── Eager fetch with info (avoids N+1 on list views) ───────────

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.info LEFT JOIN FETCH c.addresses")
    List<Customer> findAllWithDetails();

    @Query(value="SELECT * FROM customers",nativeQuery = true)
    List<Customer> findAllIncludingDeleted();

    @Query(value=" SELECT * FROM customers WHERE customer_id=:id",nativeQuery = true)
    Customer findByIdIncludingDeleted(@Param("id") Long id);
    @Query(value = "SELECT * FROM customers WHERE is_deleted = true", nativeQuery = true)
    List<Customer> findAllDeleted();

    /**
     * Checks if an email belongs to any customer (including soft-deleted).
     * Prevents re-registration with the same email after soft-deletion.
     */
//    @Query(value = "SELECT COUNT(*) > 0 FROM users u " +
//            "JOIN customers c ON u.id = c.user_id " +
//            "WHERE u.email = :email",
//            nativeQuery = true)
//    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
//    boolean existsByEmailIncludingDeleted(@Param("email") String email);

    /**
     * Bulk soft-delete all customers in a branch (used when deactivating a branch).
     * This is a direct UPDATE — does not trigger JPA entity lifecycle events.
     * Use only for bulk operations; prefer entity-level softDelete() for single records.
     */
    @Modifying
    @Query("""
    UPDATE Customer c
    SET c.isDeleted = true,
        c.deletedAt = CURRENT_TIMESTAMP,
        c.deletedBy = :deletedBy
    WHERE c.branchId = :branchId
""")
    int softDeleteAllByBranchId(@Param("branchId") Long branchId,
                                @Param("deletedBy") String deletedBy);


}
