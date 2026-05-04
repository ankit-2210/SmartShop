package com.ecommerce.service.Impl;

import com.ecommerce.model.Users.Products.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public Product saveProduct(Product product){

        return productRepository.save(product);
    }

    @Override
    public Page<Product> getAllProduct(Integer pageNo, Integer pagesize){
        Pageable pageable= PageRequest.of(pageNo, pagesize);
        return productRepository.findAll(pageable);
    }

    @Override
    public Boolean deleteProduct(Long id){
        Product product=productRepository.findById(id).orElse(null);

        if(!ObjectUtils.isEmpty(product)){
            productRepository.delete(product);
            return true;
        }

        return false;
    }

    @Override
    public Product getProductById(Long id){
        Product product=productRepository.findById(id).orElse(null);
        return product;
    }

    @Override
    public Page<Product> getAllActiveProducts(String categoryName, String subcategoryName, Integer pageNo, Integer pagesize){
        Pageable pageable= PageRequest.of(pageNo, pagesize);
        Page<Product> products=ObjectUtils.isEmpty(categoryName) || ObjectUtils.isEmpty(subcategoryName) ? productRepository.findByIsActiveTrue(pageable) : productRepository.findByCategoryAndSubcategory(categoryName, subcategoryName, pageable);
        return products;
    }

    @Override
    public Page<Product> getProductsByColors(List<String> colors, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        if (colors == null || colors.isEmpty()) {
            // If no colors selected, return all active products
            return productRepository.findByIsActiveTrue(pageable);
        }
        else {
            // Filter products where any of the product's colors match the selected colors
            return productRepository.findByColorNames(colors, pageable);
        }
    }

    @Override
    public Page<Product> getProductsByCategorySubcategoryAndColors(String category, String subcategory, List<String> colors, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        // Combine filters using repository or CriteriaBuilder
        return productRepository.findByCategorySubcategoryAndColors(category, subcategory, colors, pageable);
    }

    @Override
    public Page<Product> getProductsByPriceRange(Double minPrice, Double maxPrice, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable);
    }

    @Override
    public Page<Product> getProductsByFilters(String category, String subcategory, List<String> colors, Double minPrice, Double maxPrice, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepository.findByFilters(category != null ? category : "", subcategory != null ? subcategory : "", (colors != null && !colors.isEmpty()) ? colors : null, minPrice, maxPrice, pageable);
    }

    @Override
    public void deactivateProductsByCategory(String categoryName, String subcategoryName, Integer pageNo, Integer pagesize) {
        Pageable pageable= PageRequest.of(pageNo, pagesize);
        Page<Product> products = productRepository.findByCategoryAndSubcategory(categoryName, subcategoryName, pageable);
        for (Product p : products) {
            p.setIsActive(false);
        }
        productRepository.saveAll(products.getContent());
    }

    // ✅ Search by name/description
    @Override
    public Page<Product> searchProduct(String keyword, Integer pageNo, Integer pagesize) {
        Pageable pageable= PageRequest.of(pageNo, pagesize);
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);
    }

    // ✅ Search by category/subcategory
    @Override
    public Page<Product> searchByCategoryOrSubcategory(String keyword, Integer pageNo, Integer pagesize) {
        Pageable pageable= PageRequest.of(pageNo, pagesize);
        return productRepository.findByCategoryContainingIgnoreCaseOrSubcategoryContainingIgnoreCase(keyword, keyword, pageable);
    }

    @Override
    public Double getAverageRating(Long productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        return (avg != null) ? avg : 0.0;
    }


    @Override
    public Product getProductWithRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Double avgRating = reviewRepository.findAverageRatingByProductId(productId);
        Long totalReviews = reviewRepository.countReviewsByProductId(productId);

        product.setAverageRating(avgRating != null ? avgRating : 0.0);
        product.setReviewCount(totalReviews != null ? totalReviews : 0L);

        return product;
    }


}
