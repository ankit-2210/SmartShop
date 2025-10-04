package com.ecommerce.repository;

import com.ecommerce.model.Users.Products.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findByCategoryAndSubcategory(String category, String subcategory, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

    Page<Product> findByCategoryContainingIgnoreCaseOrSubcategoryContainingIgnoreCase(String category, String subcategory, Pageable pageable);

}
