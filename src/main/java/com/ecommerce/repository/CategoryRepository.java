package com.ecommerce.repository;

import com.ecommerce.model.Users.Products.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface CategoryRepository extends JpaRepository<Category, Long>  {

    boolean existsByCategoryName(String categoryName);

    Category findByCategoryName(String categoryName);

    // ✅ fetch categories that have at least one active subcategory
    List<Category> findDistinctBySubCategoriesIsActiveTrue();



}
