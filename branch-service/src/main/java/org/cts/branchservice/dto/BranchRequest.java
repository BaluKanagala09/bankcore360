package org.cts.branchservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BranchRequest {

    @NotBlank(message = "Branch code is required")
    @Size(max = 50)
    private String branchCode;

    @NotBlank(message = "Branch name is required")
    @Size(max = 150)
    private String branchName;

    @NotBlank(message = "Address is required")
    private String address;

    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be 10 digits")
    private String contactNumber;

    private Long branchManagerId;
}
