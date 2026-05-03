package org.cts.customerservice.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerNotificationResponse {

    private Long customerId;
    private String name;
    private String email;
    private List<String> deviceTokens;

}
