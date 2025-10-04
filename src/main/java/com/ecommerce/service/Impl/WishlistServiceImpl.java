package com.ecommerce.service.Impl;


import com.ecommerce.model.Users.Products.Product;
import com.ecommerce.model.Users.User;
import com.ecommerce.model.Users.Wishlist.Wishlist;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.WishlistRepository;
import com.ecommerce.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistServiceImpl implements WishlistService {
    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public boolean addToWishlist(Long userId, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            return false; // already in wishlist
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);

        wishlistRepository.save(wishlist);
        return true;
    }

    // ✅ Remove product from wishlist
    @Override
    public boolean removeFromWishlist(Long userId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId);
        if (wishlist != null) {
            wishlistRepository.delete(wishlist);
            return true;
        }
        return false; // not found
    }

    // ✅ Get all wishlist items for a user
    @Override
    public List<Wishlist> getWishlistByUser(Long userId) {
        return wishlistRepository.findAllByUserId(userId);
    }
}
