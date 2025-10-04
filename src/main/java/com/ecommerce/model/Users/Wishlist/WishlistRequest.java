package com.ecommerce.model.Users.Wishlist;

import lombok.*;

@Data
@ToString
public class WishlistRequest {

    private Long userId;
    private Long productId;

    // getters & setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

}
