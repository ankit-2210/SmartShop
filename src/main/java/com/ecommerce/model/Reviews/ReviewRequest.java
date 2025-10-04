package com.ecommerce.model.Reviews;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ReviewRequest {
    private List<SingleReview> reviews;

    public List<SingleReview> getReviews() {
        return reviews;
    }
    public void setReviews(List<SingleReview> reviews) {
        this.reviews = reviews;
    }

}
