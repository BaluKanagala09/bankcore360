package org.cts.customerservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cts.customerservice.dto.CustomerDeletedAuditResponse;
import org.cts.customerservice.dto.CustomerRegistrationRequest;
import org.cts.customerservice.dto.CustomerResponse;
import org.cts.customerservice.dto.KycUpdateRequest;
import org.cts.customerservice.service.CustomerService;
import org.cts.customerservice.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management",
        description = "Customer registration, KYC, soft delete, and restore APIs")
public class CustomerController {

    private final CustomerService customerService;

    // ── POST /customers/register ────────────────────────────────────

    @Operation(
            summary     = "Register a new customer",
            description = "Public endpoint. KYC defaults to PENDING. " +
                    "Aadhaar is masked per RBI guidelines before storage.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerResponse>> register(
            @Valid @RequestBody CustomerRegistrationRequest request) {

        log.info("POST /customers/register — email: {}", request.getEmail());
        CustomerResponse response = customerService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Customer registered successfully. KYC verification is pending.",
                        response));
    }

    // ── GET /customers ──────────────────────────────────────────────

    @Operation(summary = "Get all active customers")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAll() {
        log.info("GET /customers");
        return ResponseEntity.ok(
                ApiResponse.success("Customers retrieved", customerService.getAllCustomers()));
    }

    // ── GET /customers/{customerId} ─────────────────────────────────

    @Operation(summary = "Get an active customer by ID")
    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'BRANCH_MANAGER', 'CSR', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(
            @PathVariable Long customerId) {

        log.info("GET /customers/{}", customerId);
        return ResponseEntity.ok(
                ApiResponse.success("Customer retrieved",
                        customerService.getCustomerById(customerId)));
    }

    // ── GET /customers/kyc-status?status=PENDING ────────────────────

    @Operation(summary = "Filter active customers by KYC status")
    @GetMapping("/kyc-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getByKycStatus(
            @RequestParam String status) {

        log.info("GET /customers/kyc-status?status={}", status);
        return ResponseEntity.ok(
                ApiResponse.success("Customers filtered by KYC status",
                        customerService.getByKycStatus(status)));
    }

    // ── PATCH /customers/{customerId}/kyc ───────────────────────────

    @Operation(
            summary     = "Update KYC status",
            description = "REJECTED requires a rejectionReason in the request body.")
    @PatchMapping("/{customerId}/kyc")
    @PreAuthorize("hasAnyRole('CSR','COMPLIANCE_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateKyc(
            @PathVariable Long customerId,
            @Valid @RequestBody KycUpdateRequest request) {

        log.info("PATCH /customers/{}/kyc — status: {}", customerId, request.getKycStatus());
        return ResponseEntity.ok(
                ApiResponse.success("KYC status updated",
                        customerService.updateKycStatus(customerId, request)));
    }

    // ── DELETE /customers/{customerId} ──────────────────────────────

    @Operation(
            summary     = "Soft-delete a customer",
            description = "Marks the customer as deleted (isDeleted=true). " +
                    "The row is NEVER physically removed — RBI data retention compliance. " +
                    "CustomerInfo and Addresses are also soft-deleted in the same transaction " +
                    "without requiring a separate CustomerInfoRepository.")
    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> softDelete(@PathVariable Long customerId) {
        log.info("DELETE /customers/{} (soft delete)", customerId);
        customerService.softDeleteCustomer(customerId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer soft-deleted successfully. Record retained in database for audit."));
    }

    // ── PATCH /customers/{customerId}/restore ───────────────────────

    @Operation(
            summary     = "Restore a soft-deleted customer",
            description = "Reverses a soft deletion. Clears isDeleted, deletedAt, deletedBy " +
                    "on Customer, CustomerInfo, and Addresses. ADMIN only.")
    @PatchMapping("/{customerId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerResponse>> restore(
            @PathVariable Long customerId) {

        log.info("PATCH /customers/{}/restore", customerId);
        return ResponseEntity.ok(
                ApiResponse.success("Customer restored successfully",
                        customerService.restoreCustomer(customerId)));
    }

    // ── GET /customers/deleted ──────────────────────────────────────

    @Operation(
            summary     = "View all soft-deleted customers (audit)",
            description = "Returns deleted customer records including deletedAt and deletedBy. " +
                    "ADMIN only — compliance and audit use.")
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CustomerDeletedAuditResponse>>> getDeleted() {
        log.info("GET /customers/deleted");
        return ResponseEntity.ok(
                ApiResponse.success("Deleted customers retrieved",
                        customerService.getDeletedCustomers()));
    }
}
