package org.com.cts.notificationservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerNotificationProfile {

    private Long customerId;
    private String name;
    private String email;
    private List<String> deviceTokens;
}

