package com.ecommerce.payload.response.Orders;

import com.ecommerce.model.Orders.OrderAddress;
import lombok.*;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    private String orderId;
    private double totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private String paymentType;
    private String paymentId;
    private Date orderDate;

    private OrderAddress address;
    private List<OrderItemResponse> items;
}

