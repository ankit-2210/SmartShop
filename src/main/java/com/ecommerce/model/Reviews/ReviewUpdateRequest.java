package com.ecommerce.model.Reviews;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewUpdateRequest {
    private int reviewId;      // review id to update
    private int rating;
    private String comment;
}
