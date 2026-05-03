package org.cts.customerservice.service;

//import jakarta.transaction.Transactional;
//import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// CFD
//import org.cts.bankcore360.modules.branch.entity.Branch;
//import org.cts.bankcore360.modules.branch.repository.BranchRepository;
import org.cts.customerservice.client.AuthClient;
import org.cts.customerservice.config.NotificationEventProducer;
import org.cts.customerservice.dto.*;
import org.cts.customerservice.entity.Address;
import org.cts.customerservice.entity.Customer;
import org.cts.customerservice.entity.CustomerInfo;
import org.cts.customerservice.enums.KycStatus;
import org.cts.customerservice.repository.CustomerRepository;
import org.cts.customerservice.exception.BusinessException;
import org.cts.customerservice.exception.DuplicateResourceException;
import org.cts.customerservice.exception.ResourceNotFoundException;
// CFD
//import org.cts.bankcore360.modules.user.entity.User;
//import org.cts.bankcore360.modules.user.enums.UserRole;
//import org.cts.bankcore360.modules.user.enums.UserStatus;
//import org.cts.bankcore360.modules.user.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;   // single repo — no CustomerInfoRepository
//    private final UserRepository userRepository;    CFD
//    private final PasswordEncoder passwordEncoder; CFD
//    private final BranchRepository branchRepository;   CFD
    private final NotificationEventProducer notificationEventProducer;
    private final AuthClient authClient;

    // ════════════════════════════════════════════════════════════════
    // REGISTRATION
    // ════════════════════════════════════════════════════════════════

    /**
     * Registers a new customer.
     *
     * Guards against:
     *  - duplicate email (including previously soft-deleted accounts)
     *  - duplicate Aadhaar (masked form checked in DB)
     *  - duplicate PAN
     *
     * Aadhaar is masked to "XXXX-XXXX-1234" BEFORE being stored (RBI FIX #4).
     * CustomerInfo and Addresses are persisted via CascadeType.ALL (FIX #1 — no extra repo).
     */
    @Transactional
    public CustomerResponse registerCustomer(CustomerRegistrationRequest request) {
        log.info("Registering customer — email: {}", request.getEmail());

//        CFD
//        Branch branch=branchRepository.findById(request.getBranchId()).orElseThrow(
//                ()-> new ResourceNotFoundException("Branch","id",request.getBranchId())
//        );
        String maskedAadhar = maskAadhar(request.getAadhar());

        if (customerRepository.existsByInfoAadhar(maskedAadhar)) {
            throw new DuplicateResourceException("Aadhaar number is already registered.");
        }

        String maskedPan=maskedPan(request.getPan());
        if (customerRepository.existsByInfoPan(maskedPan)){
            throw new DuplicateResourceException(
                    "PAN number is already registered: " + maskedPan);
        }

        // 1. Create login User
       UserResponse user=authClient.createCustomerUser(
              Map.of(
                      "email",request.getEmail(),
                      "password",request.getPassword(),
                      "fullName", request.getFirstName() + " " + request.getLastName(),
                      "phoneNumber", request.getPhoneNumber()
              )
       ).getData();

        // 2. Customer shell
        Customer customer = Customer.builder()
                .userId(user.getId())
                .branchId(user.getBranchId())
                .kycStatus(KycStatus.PENDING)
                .build();

        // 3. CustomerInfo — Aadhaar already masked
        CustomerInfo info = CustomerInfo.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dob(request.getDob())
                .aadhar(maskedAadhar)                  // ← masked before save
                .pan(maskedPan)
                .gender(request.getGender())
                .nationality(request.getNationality())
                .phoneNumber(request.getPhoneNumber())
                .customer(customer)
                .build();

        // 4. Addresses
        List<Address> addresses = request.getAddresses().stream()
                .map(a -> Address.builder()
                        .addressType(a.getAddressType())
                        .street(a.getStreet())
                        .city(a.getCity())
                        .state(a.getState())
                        .country(a.getCountry())
                        .pinCode(a.getPinCode())
                        .customer(customer)
                        .build())
                .collect(Collectors.toList());

        customer.setInfo(info);
        customer.setAddresses(addresses);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        // 5. Single save — cascade handles CustomerInfo and Addresses (no extra repo)
        Customer saved = customerRepository.save(customer);

        // Notification Logic
        notificationEventProducer.publish(
                NotificationEvent.builder()
                        .eventType("CUSTOMER_REGISTERED")
                        .customerId(saved.getCustomerId())
                        .timestamp(LocalDateTime.now())
                .build()
        );
        log.info("Customer registered — customerId={}", saved.getCustomerId());
        return toResponse(saved);
    }

    // ════════════════════════════════════════════════════════════════
    // READ (active records only — @SQLRestriction auto-filters deleted)
    // ════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long customerId) {
        return toResponse(findActiveById(customerId));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAllWithDetails().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getByKycStatus(String status) {
        KycStatus kycStatus = parseKycStatus(status);
        return customerRepository.findByKycStatus(kycStatus).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════
    // KYC
    // ════════════════════════════════════════════════════════════════

    /**
     * Updates KYC status. REJECTED requires a reason.
     * Enforced at controller level via @PreAuthorize("hasRole('COMPLIANCE_OFFICER')").
     */
    @Transactional
    public CustomerResponse updateKycStatus(Long customerId, KycUpdateRequest request) {
        log.info("KYC update — customerId={}, newStatus={}", customerId, request.getKycStatus());

        Customer customer = findActiveById(customerId);
        KycStatus newStatus = parseKycStatus(request.getKycStatus());

        if (newStatus == KycStatus.REJECTED
                && (request.getRejectionReason() == null
                || request.getRejectionReason().isBlank())) {
            throw new BusinessException(
                    "Rejection reason is mandatory when setting KYC status to REJECTED.");
        }

        customer.setKycStatus(newStatus);
        customerRepository.save(customer);

        //Notification Logic
        notificationEventProducer.publish(
                NotificationEvent.builder()
                        .eventType("KYC_APPROVED")
                        .customerId(customerId)
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        log.info("KYC updated — customerId={} → {}", customerId, newStatus);
        return toResponse(customer);
    }

    // ════════════════════════════════════════════════════════════════
    // SOFT DELETE
    // ════════════════════════════════════════════════════════════════

    /**
     * Soft-deletes a customer record.
     *
     * What happens:
     *   1. Customer.isDeleted    = true
     *   2. Customer.deletedAt    = now
     *   3. Customer.deletedBy    = currently logged-in user's email
     *   4. CustomerInfo.isDeleted = true  (no separate repo — direct field set)
     *   5. Each Address.isDeleted = true   (same — direct field set)
     *   6. customerRepository.save(customer) cascades all changes in ONE transaction.
     *
     * The row is NEVER physically deleted. RBI mandates data retention.
     *
     * @param customerId the customer to soft-delete
     */
    @Transactional
    public void softDeleteCustomer(Long customerId) {
        Customer customer = findActiveById(customerId);

        // Prevent deletion of a customer with an active loan (business rule)
        // Uncomment and wire LoanRepository when available:
        // if (loanRepository.existsByCustomerIdAndStatusIn(
        //         customerId, List.of(LoanStatus.APPROVED, LoanStatus.DISBURSED))) {
        //     throw new BusinessException("Cannot delete a customer with an active loan.");
        // }

        String deletedBy = currentUserEmail();

        // Customer.softDelete() handles Customer + CustomerInfo + Addresses in memory.
        // No CustomerInfoRepository.save() needed — cascade does it.
        customer.softDelete(deletedBy);

        customerRepository.save(customer);

        log.info("Customer soft-deleted — customerId={}, deletedBy={}", customerId, deletedBy);
    }


/**
 * Restores a previously soft-deleted customer.
 *
 * Uses findByIdIncludingDeleted
 which bypasses the @SQLRestriction filter.
            * Clears isDeleted, deletedAt, deletedBy on Customer, CustomerInfo, Addresses.
            *
            * @param customerId the customer to restore
     * @return CustomerResponse of the restored customer
     */
    @Transactional
    public CustomerResponse restoreCustomer(Long customerId) {
        // findByIdIncludingDeleted bypasses @SQLRestriction — sees deleted rows
        Customer customer = customerRepository.findByIdIncludingDeleted(customerId);
                if(customer==null){
                    throw new ResourceNotFoundException("Customer", "id", customerId);
                }


        if (!customer.getIsDeleted()) {
            throw new BusinessException(
                    "Customer " + customerId + " is not deleted — nothing to restore.");
        }

        // Restore Customer
        customer.setIsDeleted(false);
        customer.setDeletedAt(null);
        customer.setDeletedBy(null);

        // Restore CustomerInfo (no repo needed — direct field access via relationship)
        if (customer.getInfo() != null) {
            customer.getInfo().setIsDeleted(false);
            customer.getInfo().setDeletedAt(null);
            customer.getInfo().setDeletedBy(null);
        }

        // Restore Addresses
        if (customer.getAddresses() != null) {
            customer.getAddresses().forEach(addr -> {
                addr.setIsDeleted(false);
                addr.setDeletedAt(null);
                addr.setDeletedBy(null);
            });
        }

        Customer restored = customerRepository.save(customer);
        log.info("Customer restored — customerId={}, restoredBy={}",
                customerId, currentUserEmail());

        return toResponse(restored);
    }

    // ════════════════════════════════════════════════════════════════
    // AUDIT VIEW (ADMIN / COMPLIANCE_OFFICER)
    // ════════════════════════════════════════════════════════════════

    /**
     * Returns all soft-deleted customer records for audit purposes.
     * Bypasses @SQLRestriction — visible to ADMIN only.
     */
    @Transactional(readOnly = true)
    public List<CustomerDeletedAuditResponse> getDeletedCustomers() {
        return customerRepository.findAllDeleted().stream()
                .map(this::toAuditResponse)
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════
    // PACKAGE-LEVEL HELPER
    // ════════════════════════════════════════════════════════════════

    /**
     * Finds an active (not soft-deleted) customer by ID.
     * @SQLRestriction ensures deleted records are never returned.
     */
    @Transactional(readOnly = true)
    public Customer findActiveById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer", "id", customerId));
    }

    // ════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════════

    /**
     * RBI compliance — masks raw 12-digit Aadhaar to "XXXX-XXXX-NNNN".
     * Called before any DB write. Raw digits never stored.
     */
    private String maskAadhar(String raw) {
        if (raw == null || raw.length() != 12) {
            throw new BusinessException("Invalid Aadhaar — must be exactly 12 digits.");
        }
        return "XXXX-XXXX-" + raw.substring(8);
    }

    //maksing pan
    private String maskedPan(String raw) {
        if (raw == null || raw.length() != 10) {
            throw new BusinessException("Invalid PAN — must be exactly 10 characters");
        }
        return "XXXXXX" + raw.substring(6);
    }

    private KycStatus parseKycStatus(String status) {
        try {
            return KycStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid KYC status: " + status);
        }
    }

    /** Returns the email of the currently authenticated user. */
    private String currentUserEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    /** Maps Customer → CustomerResponse DTO. */
    private CustomerResponse toResponse(Customer c) {
        CustomerInfo info = c.getInfo();
        Long branchId=c.getBranchId();
        List<AddressResponse> addrList = c.getAddresses() == null ? List.of()
                : c.getAddresses().stream()
                .map(a -> AddressResponse.builder()
                        .id(a.getId())
                        .addressType(a.getAddressType())
                        .street(a.getStreet())
                        .city(a.getCity())
                        .state(a.getState())
                        .country(a.getCountry())
                        .pinCode(a.getPinCode())
                        .build())
                .collect(Collectors.toList());

        return CustomerResponse.builder()
                .customerId(c.getCustomerId())
                .fullName(c.getFullName())
                .email(null)
                .branchId(branchId)
                .kycStatus(c.getKycStatus().name())
                .dob(info != null ? info.getDob() : null)
                .aadhar(info != null ? info.getAadhar() : null)   // already masked in DB
                .pan(info != null ? info.getPan() : null)
                .gender(info != null ? info.getGender() : null)
                .nationality(info != null ? info.getNationality() : null)
                .phoneNumber(info != null ? info.getPhoneNumber() : null)
                .addresses(addrList)
                .createdAt(c.getCreatedAt())
                .build();
    }

    /** Maps a soft-deleted Customer → audit DTO (includes deletion metadata). */
    private CustomerDeletedAuditResponse toAuditResponse(Customer c) {
        return CustomerDeletedAuditResponse.builder()
                .customerId(c.getCustomerId())
                .fullName(c.getFullName())
                .email(null)
                .kycStatus(c.getKycStatus().name())
                .isDeleted(c.getIsDeleted())
                .deletedAt(c.getDeletedAt())
                .deletedBy(c.getDeletedBy())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
