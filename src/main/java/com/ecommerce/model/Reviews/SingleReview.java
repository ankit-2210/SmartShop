package com.ecommerce.model.Reviews;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleReview {
    private int productId;
    private int orderId;
    private int rating;
    private String comment;

}
