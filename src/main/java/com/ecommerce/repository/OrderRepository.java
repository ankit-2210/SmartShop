package com.ecommerce.repository;

import com.ecommerce.model.Orders.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderRepository extends JpaRepository<Order, Long> {

    public Order findByOrderId(String orderId);

    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndOrderStatusIgnoreCase(Long userId, String status, Pageable pageable);

    long countByUserId(Long userId);
    

}
