package com.ecommerce.service.Impl;

import com.ecommerce.model.Users.Cart.Cart;
import com.ecommerce.model.Users.Products.Product;
import com.ecommerce.model.Users.User;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Cart saveCart(Long productId, Long userId, String color, String size) {
        User user=userRepository.findById(userId).get();
        Product product=productRepository.findById(productId).get();

        // ✅ Default color if null or empty
        if (color == null || color.trim().isEmpty()) {
            color = "Midnight Black";
        }

        if(size == null || size.trim().isEmpty()){
            size="M";
        }

        Cart cart=null;
        Cart cartStatus=cartRepository.findByProductIdAndUserId(productId, userId);
        // if no user is there
        if(ObjectUtils.isEmpty(cartStatus)){
            cart=new Cart();
            cart.setProduct(product);
            cart.setUser(user);
            cart.setQuantity(1);
            cart.setTotalPrice(1*product.getDiscountPrice());
            cart.setColor(color);
            cart.setSize(size);
        }
        else{
            cart=cartStatus;
            cart.setQuantity(cartStatus.getQuantity()+1);
            cart.setTotalPrice(cart.getQuantity()*cart.getProduct().getDiscountPrice());

            if (cart.getColor() == null) {
                cart.setColor(color);
            }

            if(cart.getSize() == null){
                cart.setSize(size);
            }
        }

        Cart saveCart=cartRepository.save(cart);
        return saveCart;
    }

    @Override
    public List<Cart> getCartsByUser(Long userId) {

        List<Cart> carts = cartRepository.findByUserId(userId);
        List<Cart> updateCarts=new ArrayList<>();
        Double totalOrderPrice=0.0;
        for(Cart c: carts){
            Double totalPrice = (c.getProduct().getDiscountPrice()*c.getQuantity());
            c.setTotalPrice(totalPrice);

            totalOrderPrice += totalPrice;
            c.setTotalOrderPrice(totalOrderPrice);
            updateCarts.add(c);
        }

        return updateCarts;
    }

    @Override
    public Integer getCountCart(Long userId){
        Integer countByUserId = cartRepository.countByUserId(userId);
        return countByUserId;
    }


    @Override
    public Cart changeQuantity(Long pid, Long uid, int delta) {
        // get cart for product + user
        Cart cart = cartRepository.findByProductIdAndUserId(pid, uid);

        if (cart == null) {
            return null; // nothing to update
        }

        int newQty = cart.getQuantity() + delta;
        if (newQty <= 0) {
            // remove if quantity goes to 0
            cartRepository.delete(cart);
            return null;
        }
        else {
            cart.setQuantity(newQty);
            return cartRepository.save(cart);
        }
    }

    @Override
    public int getQuantityByProduct(Long pid, Long uid) {
        Cart cart = cartRepository.findByProductIdAndUserId(pid, uid);
        return cart != null ? cart.getQuantity() : 0;
    }

    @Override
    public double getItemSubtotal(Long pid, Long uid) {
        Cart cart = cartRepository.findByProductIdAndUserId(pid, uid);
        return cart != null ? cart.getQuantity() * cart.getProduct().getPrice() : 0.0;
    }


    @Override
    public double getCartTotalByUser(Long uid) {
        List<Cart> carts = cartRepository.findByUserId(uid);
        return carts.stream()
                .mapToDouble(c -> c.getProduct().getDiscountPrice() * c.getQuantity())
                .sum();
    }

    @Override
    public boolean removeFromCart(Long pid, Long uid) {
        Cart cart = cartRepository.findByProductIdAndUserId(pid, uid);
        if (cart != null) {
            cartRepository.delete(cart);
            return true;
        }
        return false;
    }


    @Override
    public void clearCartByUser(Long uid) {
        cartRepository.deleteByUserId(uid);
    }



    @Override
    public void updateShipping(Long userId, String shippingMethod) {
        double shippingPrice = 0.0;

        switch (shippingMethod) {
            case "standard":
                shippingPrice = 4.99;
                break;
            case "express":
                shippingPrice = 12.99;
                break;
            case "free":
                shippingPrice = 0.0;
                break;
        }

        List<Cart> carts = cartRepository.findByUserId(userId);
        for (Cart cart : carts) {
            cart.setShippingType(shippingMethod);
            cart.setShippingDeliveryPrice(shippingPrice);
            cartRepository.save(cart);
        }
    }

    @Override
    public String getUserShippingType(Long userId) {
        return cartRepository.findByUserId(userId).stream()
                .findFirst()
                .map(cart -> cart.getShippingType().toLowerCase())
                .orElse("standard");
    }

    @Override
    public double getUserShippingCost(Long userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);
        if (!carts.isEmpty()) {
            return carts.get(0).getShippingDeliveryPrice(); // all rows have same shipping
        }
        return 0.0;
    }



}
