package org.cts.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenRequest {

    @NotBlank
    private String deviceToken;

    @NotBlank
    private String platform;

}
