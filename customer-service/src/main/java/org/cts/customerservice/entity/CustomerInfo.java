package org.cts.customerservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cts.customerservice.auditlogs.Auditable;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Table(name = "customer_details")
@SQLRestriction("is_deleted=false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerInfo extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(unique = true, length = 14)
    private String aadhar;

    @Column(unique=true,nullable = false)
    private String email;

    @Column(unique = true, length = 10)
    private String pan;

    @Column(length = 10)
    private String gender;

    @Column(length = 50)
    private String nationality;

    @Column(name="phone_number",length=15)
    private String phoneNumber;


    // The owning side of the One-to-One relationship
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id",nullable = false,unique=true)
    private Customer customer;
}
