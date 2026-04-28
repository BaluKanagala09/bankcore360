package org.cts.customerservice.dto;
//package org.cts.bankcore360.modules.customer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRegistrationRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2–50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be 2–50 characters")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^[2-9][0-9]{11}$", message = "Aadhaar must be exactly 12 digits")
    private String aadhar;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN must follow format: ABCDE1234F")
    private String pan;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Nationality is required")
    private String nationality;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Enter a valid 10-digit Indian mobile number")
    private String phoneNumber;


    @NotNull(message = "Branch selection is mandatory")
    private Long branchId;

    // ── Login Credentials ──────────────────────────────────────────

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // ── Addresses ──────────────────────────────────────────────────

    @NotNull(message = "At least one address is required")
    @Size(min = 1, max = 2, message = "Provide 1 or 2 addresses (CURRENT and/or PERMANENT)")
    @Valid
    private List<AddressRequest> addresses;

}