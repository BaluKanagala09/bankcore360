package org.cts.transactionservice.stub;

import org.cts.transactionservice.dto.response.BranchDto;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stub simulating BranchService communication.
 * In production this would be a Feign client calling the branch-service.
 */
@Service
public class BranchServiceClient {

    private static final Map<Long, BranchDto> BRANCHES = new HashMap<>();

    static {
        BRANCHES.put(1L, new BranchDto(1L, "Main Street Branch", "New York"));
        BRANCHES.put(2L, new BranchDto(2L, "Downtown Branch", "Los Angeles"));
        BRANCHES.put(3L, new BranchDto(3L, "Uptown Branch", "Chicago"));
    }

    public Optional<BranchDto> getBranchById(Long branchId) {
        return Optional.ofNullable(BRANCHES.get(branchId));
    }

    public boolean branchExists(Long branchId) {
        return BRANCHES.containsKey(branchId);
    }
}

