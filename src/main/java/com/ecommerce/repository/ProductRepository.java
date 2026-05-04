package com.ecommerce.repository;

import com.ecommerce.model.Users.Products.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findByCategoryAndSubcategory(String category, String subcategory, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p JOIN p.colors c " +
            "WHERE c.colorName IN :colorNames AND p.isActive = true")
    Page<Product> findByColorNames(@Param("colorNames") List<String> colorNames, Pageable pageable);

    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN p.colors c
        WHERE p.isActive = true
        AND (:categoryName IS NULL OR :categoryName = '' OR p.category = :categoryName)
        AND (:subcategoryName IS NULL OR :subcategoryName = '' OR p.subcategory = :subcategoryName)
         AND (:colors IS NULL OR c.colorName IN :colors)
    """)
    Page<Product> findByCategorySubcategoryAndColors(@Param("categoryName") String categoryName, @Param("subcategoryName") String subcategoryName, @Param("colors") List<String> colors, Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE p.isActive = true
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    """)
    Page<Product> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);


    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN p.colors c
        WHERE p.isActive = true
        AND (:categoryName = '' OR p.category = :categoryName)
        AND (:subcategoryName = '' OR p.subcategory = :subcategoryName)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:colors IS NULL OR c.colorName IN :colors)
    """)
    Page<Product> findByFilters(@Param("categoryName") String categoryName, @Param("subcategoryName") String subcategoryName, @Param("colors") List<String> colors, @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);


    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

    Page<Product> findByCategoryContainingIgnoreCaseOrSubcategoryContainingIgnoreCase(String category, String subcategory, Pageable pageable);

}
