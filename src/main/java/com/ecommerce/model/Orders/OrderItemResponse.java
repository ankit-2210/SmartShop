package com.ecommerce.model.Orders;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private Long productId;     // 🔑 helps frontend if you need to link to product detail page
    private String productName; // name of product
    private int quantity;       // quantity purchased
    private double price;       // unit price (not total)

}