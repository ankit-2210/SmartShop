package com.ecommerce.service;

import com.ecommerce.model.Users.Products.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    public Product saveProduct(Product product);

    public Page<Product> getAllProduct(Integer pageNo, Integer pagesize);

    public Boolean deleteProduct(Long id);

    public Product getProductById(Long id);

    public Page<Product> getAllActiveProducts(String category, String subcategory, Integer pageNo, Integer pagesize);

    public Page<Product> getProductsByColors(List<String> colors, Integer pageNo, Integer pageSize);

    public Page<Product> getProductsByCategorySubcategoryAndColors(String category, String subcategory, List<String> colors, Integer pageNo, Integer pageSize);

    public Page<Product> getProductsByPriceRange(Double minPrice, Double maxPrice, int pageNo, int pageSize);

    public Page<Product> getProductsByFilters(String category, String subcategory, List<String> colors, Double minPrice, Double maxPrice, int pageNo, int pageSize);


    public void deactivateProductsByCategory(String categoryName, String subcategoryName, Integer pageNo, Integer pagesize);

    public Page<Product> searchProduct(String keyword, Integer pageNo, Integer pagesize);

    public Page<Product> searchByCategoryOrSubcategory(String keyword, Integer pageNo, Integer pagesize);

    public Double getAverageRating(Long productId);

    public Product getProductWithRating(Long productId);
}
