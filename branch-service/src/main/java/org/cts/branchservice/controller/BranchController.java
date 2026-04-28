package org.cts.branchservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cts.branchservice.dto.BranchRequest;
import org.cts.branchservice.dto.BranchResponse;
import org.cts.branchservice.service.BranchService;
//import org.cts.bankcore360.modules.utils.ApiResponse;
import org.cts.branchservice.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/branches")
@RequiredArgsConstructor
@Tag(name = "Branch Management", description = "CRUD operations for bank branches")
public class BranchController {

    private final BranchService branchService;

    @Operation(summary = "Create a new branch")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @Valid @RequestBody BranchRequest request) {
        log.info("POST /branches — code: {}", request.getBranchCode());
        BranchResponse response = branchService.createBranch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Branch created successfully", response));
    }

    @Operation(summary = "Get all branches")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
//    @PreAuthorize("permitAll()")

    public ResponseEntity<ApiResponse<List<BranchResponse>>> getAllBranches() {
        log.info("GET /branches");
        return ResponseEntity.ok(ApiResponse.success("Branches retrieved", branchService.getAllBranches()));
    }

    @Operation(summary = "Get active branches only")
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getActiveBranches() {
        log.info("GET /branches/active");
        return ResponseEntity.ok(ApiResponse.success("Active branches retrieved", branchService.getActiveBranches()));
    }

    @Operation(summary = "Get branch by ID")
    @GetMapping("/{branchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable Long branchId) {
        log.info("GET /branches/{}", branchId);
        return ResponseEntity.ok(ApiResponse.success("Branch retrieved", branchService.getBranchById(branchId)));
    }

    @Operation(summary = "Update branch details")
    @PutMapping("/{branchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable Long branchId,
            @Valid @RequestBody BranchRequest request) {
        log.info("PUT /branches/{}", branchId);
        return ResponseEntity.ok(ApiResponse.success("Branch updated", branchService.updateBranch(branchId, request)));
    }

//    @Operation(summary = "Deactivate a branch")
//    @DeleteMapping("/{branchId}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ApiResponse<Void>> deactivateBranch(@PathVariable Long branchId) {
//        log.info("DELETE /branches/{}", branchId);
//        branchService.deactivateBranch(branchId);
//        return ResponseEntity.ok(ApiResponse.success("Branch deactivated successfully"));
//    }
}
