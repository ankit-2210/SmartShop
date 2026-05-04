package com.ecommerce.repository;

import com.ecommerce.model.Users.Products.Brand;
import com.ecommerce.model.Users.Products.SubCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findBySubCategory(SubCategory subCategory);

    List<Brand> findBySubCategory_Id(Long subCategoryId);


    @Query("SELECT b AS brand, COUNT(p.id) AS productCount " +
            "FROM Brand b JOIN Product p ON p.brand = b " +
            "GROUP BY b " +
            "ORDER BY COUNT(p.id) DESC")
    List<Object[]> findTopBrands(Pageable pageable);


}
