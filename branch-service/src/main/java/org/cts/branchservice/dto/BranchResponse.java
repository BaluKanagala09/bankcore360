package org.cts.branchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {
    private Long id;
    private String branchCode;
    private String branchName;
    private String address;
    private String contactNumber;
    private Long branchManagerId;
    private Boolean isActive;
}