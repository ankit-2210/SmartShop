package com.ecommerce.service;

import com.ecommerce.model.Users.Products.Product;
import org.springframework.data.domain.Page;

public interface ProductService {

    public Product saveProduct(Product product);

    public Page<Product> getAllProduct(Integer pageNo, Integer pagesize);

    public Boolean deleteProduct(Long id);

    public Product getProductById(Long id);

    public Page<Product> getAllActiveProducts(String category, String subcategory, Integer pageNo, Integer pagesize);

    public void deactivateProductsByCategory(String categoryName, String subcategoryName, Integer pageNo, Integer pagesize);

    public Page<Product> searchProduct(String keyword, Integer pageNo, Integer pagesize);

    public Page<Product> searchByCategoryOrSubcategory(String keyword, Integer pageNo, Integer pagesize);

    public Double getAverageRating(Long productId);

    public Product getProductWithRating(Long productId);
}
