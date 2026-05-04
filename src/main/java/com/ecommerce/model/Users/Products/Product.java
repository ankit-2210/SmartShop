package com.ecommerce.model.Users.Products;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Product title is required")
    @Column(length = 500)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(length = 5000)
    private String description;

    private String category;        // Category name
    private String subcategory;     // Subcategory name

    @NotNull(message = "Price is required")
    @Min(value = 1, message = "Price must be greater than 0")
    private Double price;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;          // Total stock

    private String image;           // Image filename
    private int discount;           // Discount in %
    private Double discountPrice;   // Price after discount
    private Boolean isActive;       // Active/Inactive

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Transient
    private Double averageRating;

    @Transient
    private Long reviewCount;

    // Colors
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductColor> colors = new ArrayList<>();

    // Sizes (for clothing products)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSize> sizes = new ArrayList<>();

    @Transient
    private Map<String, String> colorHexMap = new HashMap<>();

    public Map<String, String> getColorHexMap() {
        return colorHexMap;
    }

    public void setColorHexMap(Map<String, String> colorHexMap) {
        this.colorHexMap = colorHexMap;
    }

    @Transient
    private Map<String, Integer> sizeStockMap = new HashMap<>();

    // Compute total stock from sizes
    public void updateStockFromSizes() {
        if (sizes != null && !sizes.isEmpty()) {
            this.stock = sizes.stream().mapToInt(ProductSize::getStock).sum();
        }
    }
}
