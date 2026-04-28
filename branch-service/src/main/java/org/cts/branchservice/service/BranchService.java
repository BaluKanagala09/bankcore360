package org.cts.branchservice.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cts.branchservice.dto.BranchRequest;
import org.cts.branchservice.dto.BranchResponse;
import org.cts.branchservice.entity.Branch;
import org.cts.branchservice.repository.BranchRepository;
import org.cts.branchservice.exception.DuplicateResourceException;
import org.cts.branchservice.exception.ResourceNotFoundException;
import org.cts.branchservice.dto.BranchRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for branch creation and management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        log.info("Creating branch: {}", request.getBranchCode());

        if (branchRepository.existsByBranchCode(request.getBranchCode())) {
            throw new DuplicateResourceException(
                    "Branch already exists with code: " + request.getBranchCode());
        }

        Branch branch = Branch.builder()
                .branchCode(request.getBranchCode())
                .branchName(request.getBranchName())
                .address(request.getAddress())
                .contactNumber(request.getContactNumber())
                .branchManagerId(request.getBranchManagerId())
                .isActive(true)
                .build();

        Branch saved = branchRepository.save(branch);
        log.info("Branch created: id={}, code={}", saved.getId(), saved.getBranchCode());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getActiveBranches() {
        return branchRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        return mapToResponse(branch);
    }

    @Transactional
    public BranchResponse updateBranch(Long id, BranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));

        branch.setBranchName(request.getBranchName());
        branch.setAddress(request.getAddress());
        branch.setContactNumber(request.getContactNumber());
        branch.setBranchManagerId(request.getBranchManagerId());

        Branch saved = branchRepository.save(branch);
        log.info("Branch updated: id={}", id);
        return mapToResponse(saved);
    }

//    @Transactional
//    public void deactivateBranch(Long id) {
//        Branch branch = branchRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
//
//        if (!branch.getIsActive()) {
//            throw new BusinessException("Branch is already inactive.");
//        }
//
//        branch.setIsActive(false);
//        branchRepository.save(branch);
//        log.info("Branch deactivated: id={}", id);
//    }

    private BranchResponse mapToResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .branchCode(branch.getBranchCode())
                .branchName(branch.getBranchName())
                .address(branch.getAddress())
                .contactNumber(branch.getContactNumber())
                .branchManagerId(branch.getBranchManagerId())
                .isActive(branch.getIsActive())
                .build();
    }
}
