package org.cts.accountservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountRequest {

    @NotNull(message = "Customer id is reqd")
    private Long customerId;

    @NotBlank(message="Account type is required (SAVINGS or CURRENT)")
    private  String accountType;

    @NotNull(message= "BranchId is required")
    private Long branchId;


    @Min(value=0,message="Initial deposit cannot be negative")
    private Double initialDeposit=0.0;


}
