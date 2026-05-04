package com.ecommerce.service.Impl;

import com.ecommerce.model.Orders.Order;
import com.ecommerce.model.Orders.OrderAddress;
import com.ecommerce.model.Orders.OrderItem;
import com.ecommerce.payload.request.Orders.OrderRequest;
import com.ecommerce.model.Users.Cart.Cart;
import com.ecommerce.model.Users.Profile.User;
import com.ecommerce.repository.*;
import com.ecommerce.service.*;
import com.ecommerce.util.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void saveOrder(Long userId, OrderRequest orderRequest, com.razorpay.Order razorpayOrder, double totalAmount) {
        User user = userRepository.findById(userId).get();
        List<Cart> carts = cartRepository.findByUserId(userId);
        if (carts.isEmpty()) {
            throw new IllegalStateException("Cart is empty for user: " + userId);
        }

        // Create ONE order
        Order order = new Order();
        order.setOrderId(razorpayOrder.get("id"));
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setOrderStatus(OrderStatus.IN_PROGRESS.getName());
        order.setUser(carts.get(0).getUser()); // all cart items belong to same user
        order.setPaymentType(orderRequest.getPaymentType());

        OrderAddress orderAddress = new OrderAddress();
        orderAddress.setFirstName(orderRequest.getFirstName());
        orderAddress.setLastName(orderRequest.getLastName());
        orderAddress.setEmail(orderRequest.getEmail());
        orderAddress.setMobileNo(orderRequest.getMobileNo());
        orderAddress.setAddress(orderRequest.getAddress());
        orderAddress.setCity(orderRequest.getCity());
        orderAddress.setState(orderRequest.getState());
        orderAddress.setPincode(orderRequest.getPincode());
        orderAddress.setCountry(orderRequest.getCountry());

        order.setOrderAddress(orderAddress);

        // ✅ Convert cart items → order items
        List<OrderItem> orderItems = carts.stream().map(cart -> {
            OrderItem item = new OrderItem();
            item.setOrder(order); // link to parent order
            item.setProduct(cart.getProduct());
            item.setQuantity(cart.getQuantity());
            item.setPrice(cart.getProduct().getDiscountPrice());
            return item;
        }).toList();

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        // Save order (cascade saves items + address)
        orderRepository.save(order);

        // Optionally clear cart after checkout
        cartRepository.deleteAll(carts);

    }


    @Override
    public Page<Order> getAllOrders(Integer pageNo, Integer pagesize){
        Pageable pageable= PageRequest.of(pageNo, pagesize);
        return orderRepository.findAll(pageable);
    }

    @Override
    public Order findByOrderId(String orderId){
        return orderRepository.findByOrderId(orderId);
    }

    @Override
    public Page<Order> findOrdersByUserId(Long userId, Integer pageNo, Integer pagesize) {
        Pageable pageable= PageRequest.of(pageNo, pagesize);
        return orderRepository.findAllByUserId(userId, pageable);
    }

    @Override
    public Page<Order> findOrdersByUserIdAndStatus(Long userId, String status, int pageNo, int pageSize){
        Pageable pageable= PageRequest.of(pageNo, pageSize);
        // ✅ If status is ALL, return all orders
        if ("ALL".equalsIgnoreCase(status)) {
            return orderRepository.findAllByUserId(userId, pageable);
        }

        return orderRepository.findByUserIdAndOrderStatusIgnoreCase(userId, status, pageable);
    }

    @Override
    public long countOrdersByUserId(Long userId){
        return orderRepository.countByUserId(userId);
    }

}
