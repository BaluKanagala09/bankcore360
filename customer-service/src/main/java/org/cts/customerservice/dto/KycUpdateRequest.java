package org.cts.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** Request body for updating KYC status (used by Compliance Officer). */
@Data
public class KycUpdateRequest {

    @NotBlank(message = "KYC status is required")
    @Pattern(regexp = "^(PENDING|UNDER_REVIEW|APPROVED|REJECTED)$",
            message = "Status must be PENDING, UNDER_REVIEW, APPROVED or REJECTED")
    private String kycStatus;
    /** Mandatory when rejecting KYC */
    private String rejectionReason;
}
