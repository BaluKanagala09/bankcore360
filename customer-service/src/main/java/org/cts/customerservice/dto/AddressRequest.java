package org.cts.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressRequest {
    @NotBlank(message = "Address type is required (e.g., CURRENT, PERMANENT)")
    @Pattern(regexp = "^(CURRENT|PERMANENT)$",message="Address must be either CURRENT or PERMANENT")
    private String addressType;

    @NotBlank(message = "Street cannot be blank")
    private String street;

    @NotBlank(message = "City cannot be blank")
    private String city;

    @NotBlank(message = "State cannot be blank")
    private String state;

    @NotBlank(message = "Country cannot be blank")
    private String country;

    @NotNull(message = "Pin code is required")
    @Pattern(
            regexp = "^[1-9][0-9]{5}$",
            message="PIN code must be a valid 6-digit INDIAN PIN"
    )
    private String  pinCode;

}
