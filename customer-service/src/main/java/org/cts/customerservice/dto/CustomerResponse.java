package org.cts.customerservice.dto;

//package org.cts.bankcore360.modules.customer.dto;

//package org.cts.bankcore360.modules.customer.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private  Long customerId;
    private String fullName;// You can populate this using the transient method you created
    private String email;
    private String kycStatus;
    private LocalDate dob;
    private String aadhar;
    private String pan;
    private String gender;
    private String nationality;
    private String phoneNumber;
    private Long branchId;
    private List<AddressResponse> addresses;
    private LocalDateTime createdAt;

}