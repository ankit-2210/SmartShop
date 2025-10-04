package com.ecommerce.service;

import com.ecommerce.model.Users.Wishlist.Wishlist;

import java.util.List;

public interface WishlistService {

    public boolean addToWishlist(Long userId, Long productId);

    public boolean removeFromWishlist(Long userId, Long productId);

    public List<Wishlist> getWishlistByUser(Long userId);

}
