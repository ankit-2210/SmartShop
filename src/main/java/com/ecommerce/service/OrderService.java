package com.ecommerce.service;

import com.ecommerce.model.Orders.Order;
import com.ecommerce.payload.request.Orders.OrderRequest;
import org.springframework.data.domain.Page;

public interface OrderService {

    public void saveOrder(Long userId, OrderRequest orderRequest, com.razorpay.Order order, double totalAmount);

    public Page<Order> getAllOrders(Integer pageNo, Integer pagesize);

    public Order findByOrderId(String orderId);

    public Page<Order> findOrdersByUserId(Long userId, Integer pageNo, Integer pagesize);

    public Page<Order> findOrdersByUserIdAndStatus(Long userId, String status, int pageNo, int pageSize);

    public long countOrdersByUserId(Long userId);
}
