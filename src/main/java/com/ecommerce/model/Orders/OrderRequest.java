package com.ecommerce.model.Orders;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OrderRequest {

    private double amount;       // total amount in rupees (e.g. 186.99)

    private String firstName;
    private String lastName;
    private String email;
    private String mobileNo;

    private String address;
    private String city;
    private String state;
    private String pincode;
    private String country;

    private String paymentType;  // "razorpay", "paypal", "credit-card" etc.
}
