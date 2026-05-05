package org.cts.transactionservice.clients;

import org.cts.transactionservice.dto.response.BranchDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** * Feign client for communicating with Branch Service. * Calls the branch-service microservice. */
@FeignClient(
        name = "branch-service",
        url = "${branch-service.url:http://localhost:9082}"  // Configure in application.yaml
)
public interface BranchServiceClient {
    /**     * Check if a branch exists     */
    @GetMapping("/api/v1/branches/{branchId}/exists")
    boolean branchExists(@PathVariable("branchId") Long branchId);
}