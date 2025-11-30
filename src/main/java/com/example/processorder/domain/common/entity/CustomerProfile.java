package com.example.processorder.domain.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "customerProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Address> addressList = new ArrayList<>();

    @Column(name = "total_orders", nullable = false)
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(name = "loyalty_score", nullable = false)
    @Builder.Default
    private Integer loyaltyScore = 0;

    public void addAddress(Address address) {
        addressList.add(address);
        address.setCustomerProfile(this);
    }

    public void removeAddress(Address address) {
        addressList.remove(address);
        address.setCustomerProfile(null);
    }
}
