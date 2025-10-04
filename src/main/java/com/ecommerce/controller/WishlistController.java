package com.ecommerce.controller;

import com.ecommerce.model.Users.Wishlist.Wishlist;
import com.ecommerce.model.Users.Wishlist.WishlistRequest;
import com.ecommerce.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    // ✅ Add product to wishlist
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToWishlist(@RequestBody WishlistRequest request) {
        boolean added = wishlistService.addToWishlist(request.getUserId(), request.getProductId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", added);
        response.put("message", added ? "Product added to wishlist" : "Already in wishlist");

        return ResponseEntity.ok(response);
    }

    // ✅ Remove product from wishlist
    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFromWishlist(@RequestBody WishlistRequest request) {
        boolean removed = wishlistService.removeFromWishlist(request.getUserId(), request.getProductId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", removed);
        response.put("message", removed ? "Product removed from wishlist" : "Not found in wishlist");

        return ResponseEntity.ok(response);
    }

    // ✅ Get all wishlist items by user
    @GetMapping("/{userId}")
    public ResponseEntity<List<Wishlist>> getWishlistByUser(@PathVariable Long userId) {
        List<Wishlist> wishlist = wishlistService.getWishlistByUser(userId);
        return ResponseEntity.ok(wishlist);
    }

}
