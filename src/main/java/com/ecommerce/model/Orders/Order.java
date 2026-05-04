package com.ecommerce.model.Orders;

import com.ecommerce.model.Users.Profile.User;
import com.ecommerce.util.OrderStep;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderId;

    private Date orderDate;
    private String orderStatus;
    private String paymentStatus;
    private String paymentId;
    private String paymentType;
    private String shippingType;
    private double shippingDeliveryPrice;
    private double totalAmount;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_address_id")
    private OrderAddress orderAddress;

    @Transient
    private String displayItemCount;

    @Transient
    private List<OrderStep> orderSteps = new ArrayList<>();

    @Transient
    private int currentStepIndex;

    @Transient
    private boolean allReviewed;


    // ----------------- Delegating Getters for JasperReports -----------------

    public String getFirstName() {
        return orderAddress != null ? orderAddress.getFirstName() : "";
    }

    public String getLastName() {
        return orderAddress != null ? orderAddress.getLastName() : "";
    }

    public String getAddress() {
        return orderAddress != null ? orderAddress.getAddress() : "";
    }

    public String getCity() {
        return orderAddress != null ? orderAddress.getCity() : "";
    }

    public String getState() {
        return orderAddress != null ? orderAddress.getState() : "";
    }

    public String getPincode() {
        return orderAddress != null ? orderAddress.getPincode() : "";
    }


}