package org.cts.customerservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cts.customerservice.auditlogs.Auditable;

@Entity

@Table(name = "customer_device_tokens",
      uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "deviceToken"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDeviceToken extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_token", nullable = false, length = 255)
    private String deviceToken;

    @Column(length = 20)
    private String platform; // ANDROID / IOS

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;


}
