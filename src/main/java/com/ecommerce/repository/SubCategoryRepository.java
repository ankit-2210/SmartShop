package com.ecommerce.repository;

import com.ecommerce.model.Users.Products.Category;
import com.ecommerce.model.Users.Products.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    boolean existsBySubcategoryNameAndCategory(String subcategoryName, Category category);

    List<SubCategory> findByIsActiveTrue();

}