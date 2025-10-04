package com.ecommerce.service;

import com.ecommerce.model.Users.Cart.Cart;

import java.util.List;

public interface CartService {

    public Cart saveCart(Long productId, Long userId, String color, String size);

    public List<Cart> getCartsByUser(Long userId);

    public Integer getCountCart(Long userId);

    public boolean removeFromCart(Long pid, Long uid);

    public Cart changeQuantity(Long pid, Long uid, int delta);

    public double getCartTotalByUser(Long uid);

    public int getQuantityByProduct(Long pid, Long uid);

    public double getItemSubtotal(Long pid, Long uid);

    public void clearCartByUser(Long uid);

    public void updateShipping(Long userId, String shippingMethod);

    public String getUserShippingType(Long userId);

    public double getUserShippingCost(Long userId);


}
