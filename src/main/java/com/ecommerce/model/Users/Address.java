package com.ecommerce.model.Users;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;   // Home / Office etc.

    private String fullName;

    private String phone;

    private String street;

    private String city;

    private String state;

    private String zip;

    private String country;

    private Boolean isDefault = false;

    // Each address belongs to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


}
