package org.cts.customerservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cts.customerservice.auditlogs.Auditable;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name="addresses")
@SQLRestriction("is_deleted=false")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Important: Differentiate between CURRENT and PERMANENT addresses
    @Column(name = "address_type", nullable = false, length = 20)
    private String addressType;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(name = "pin_code", nullable = false,length=6)
    private String pinCode;

    // Many addresses can belong to one Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}
