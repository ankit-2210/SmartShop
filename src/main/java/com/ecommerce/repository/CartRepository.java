package com.ecommerce.repository;

import com.ecommerce.model.Users.Cart.Cart;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {

    public Cart findByProductIdAndUserId(Long productId, Long userId);

    public Integer countByUserId(Long userId);

    public List<Cart> findByUserId(Long userId);

    @Transactional
    void deleteByUserId(Long userId);
}
