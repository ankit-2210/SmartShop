package com.ecommerce.service;

import com.ecommerce.model.Users.Products.Brand;
import com.ecommerce.payload.dto.BrandDTO;
import com.ecommerce.model.Users.Products.Category;
import com.ecommerce.model.Users.Products.SubCategory;
import org.springframework.data.domain.Page;

import java.util.*;

public interface CategoryService {

    public Category saveCategory(Category category);

    public Boolean existCategory(String categoryName);

    public boolean existsSubcategory(Category categoryName, String subcategoryName);

    public List<Category> getAllCategory();

    public Page<Category> getAllCategoryPagination(int pageNo, int pageSize);

    public Page<SubCategory> getAllSubCategoryPagination(int pageNo, int pageSize);

    public Boolean deleteCategory(Long id);

    public Category getCategoryById(Long id);

    public List<Category> getAllActiveCategory();

    Category findByCategoryAndSubcategory(String categoryName, String subcategoryName);

    public SubCategory getSubcategoryById(Long subcategoryId);

    public Boolean deleteSubCategory(Long id);


    // Brands
    Brand saveBrand(Brand brand);

    List<Brand> getBrandsBySubCategory(SubCategory subCategory);

    public List<Brand> getBrandsBySubCategoryId(Long subCategoryId);

    public List<BrandDTO> getAllBrands();

    void deleteBrand(Long id);

}
