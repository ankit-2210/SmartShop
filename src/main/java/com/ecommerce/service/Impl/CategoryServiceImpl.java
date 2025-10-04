package com.ecommerce.service.Impl;

import com.ecommerce.model.Users.Products.Category;
import com.ecommerce.model.Users.Products.SubCategory;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.SubCategoryRepository;
import com.ecommerce.service.CategoryService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Override
    public Category saveCategory(Category category){

        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategory(){

        return categoryRepository.findAll();
    }

    @Override
    public Page<Category> getAllCategoryPagination(int pageNo, int pageSize){
        Pageable pageable= PageRequest.of(pageNo, pageSize);
        return categoryRepository.findAll(pageable);
    }

    @Override
    public Page<SubCategory> getAllSubCategoryPagination(int pageNo, int pageSize){
        Pageable pageable= PageRequest.of(pageNo, pageSize);
        return subCategoryRepository.findAll(pageable);
    }


    @Override
    public Boolean existCategory(String categoryName){
        return categoryRepository.existsByCategoryName(categoryName);
    }

    @Override
    public boolean existsSubcategory(Category categoryName, String subcategoryName) {
        return subCategoryRepository.existsBySubcategoryNameAndCategory(subcategoryName, categoryName);
    }

    @Override
    public Boolean deleteCategory(Long id){
        Category category=categoryRepository.findById(id).orElse(null);
         
        if(!ObjectUtils.isEmpty(category)){
            categoryRepository.delete(category);
            return true;
        }

        return false;
    }

    @Override
    public Category getCategoryById(Long id){
        Category category=categoryRepository.findById(id).orElse(null);
        return category;
    }

    @Override
    public List<Category> getAllActiveCategory(){
        return categoryRepository.findDistinctBySubCategoriesIsActiveTrue();
    }

    @Override
    public Category findByCategoryAndSubcategory(String categoryName, String subcategoryName) {
        // Fetch category first
        Category category = categoryRepository.findByCategoryName(categoryName);
        if (category != null && category.getSubCategories() != null) {
            // Check if subcategory exists and is active
            boolean subExists = category.getSubCategories().stream()
                    .anyMatch(sub -> sub.getSubcategoryName().equals(subcategoryName) && Boolean.TRUE.equals(sub.getIsActive()));
            if(subExists) {
                return category; // return category if subcategory exists
            }
        }
        return null;
    }


    @Override
    public SubCategory getSubcategoryById(Long subcategoryId) {
        return subCategoryRepository.findById(subcategoryId).orElse(null);
    }


    @Transactional
    @Override
    public Boolean deleteSubCategory(Long id) {
        SubCategory subCategory = subCategoryRepository.findById(id).orElse(null);
        if (subCategory != null) {
            // Remove from parent collection (orphanRemoval will delete it)
            Category parent = subCategory.getCategory();
            if (parent != null) {
                parent.getSubCategories().remove(subCategory);
            }
            return true;
        }
        return false;
    }


}
