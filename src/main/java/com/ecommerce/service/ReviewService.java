package com.ecommerce.service;

import com.ecommerce.model.Reviews.Review;

import java.util.List;

public interface ReviewService {

    public Review saveReview(Long userId, Long orderId, Long productId, int rating, String comment);

    public List<Review> getReviewsByUser(Long userId);

    public List<Review> getReviewsByProduct(Long productId);


}
