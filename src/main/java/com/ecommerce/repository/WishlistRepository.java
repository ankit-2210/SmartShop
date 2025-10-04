package com.ecommerce.repository;

import com.ecommerce.model.Users.Wishlist.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    Wishlist findByUserIdAndProductId(Long userId, Long productId);
    List<Wishlist> findAllByUserId(Long userId);
}
