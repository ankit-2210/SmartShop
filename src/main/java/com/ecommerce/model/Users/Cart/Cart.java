package com.ecommerce.model.Users.Cart;

import com.ecommerce.model.Users.Products.Product;
import com.ecommerce.model.Users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Product product;

    private Integer quantity;

    @Column(nullable = false)
    private String shippingType = "standard";

    @Column(nullable = false)
    private double shippingDeliveryPrice;

    @Column(length = 100)
    private String color;

    @Column(length = 50)
    private String size;

    @Transient
    private Double totalPrice;

    @Transient
    private Double totalOrderPrice;

}
