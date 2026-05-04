package com.ecommerce.controller;

import com.ecommerce.helper.Message;

import com.ecommerce.model.Orders.Order;
import com.ecommerce.payload.dto.BrandDTO;
import com.ecommerce.payload.dto.SubCategoryDTO;
import com.ecommerce.payload.response.Orders.OrderItemResponse;
import com.ecommerce.payload.response.Orders.OrderResponse;
import com.ecommerce.model.Users.Products.*;
import com.ecommerce.model.Users.Profile.User;
import com.ecommerce.repository.*;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.UserService;

import com.ecommerce.util.EmailUtil;
import com.ecommerce.util.JasperReportUtil;
import com.ecommerce.util.OrderStatus;
import com.ecommerce.util.OrderStep;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private JasperReportUtil jasperReportUtil;


    @ModelAttribute
    public void getUserDetails(Model model, Principal principal) {
        if(principal == null) {
            model.addAttribute("User", null);
        }
        else {
            User user = userService.getUserByEmail(principal.getName());
            model.addAttribute("User", user);
        }

        List<Category> activeCategories = categoryRepository.findDistinctBySubCategoriesIsActiveTrue();
        // 3️⃣ Unique category names for filters or dropdowns
        List<String> uniqueCategories = activeCategories.stream()
                .map(Category::getCategoryName)
                .distinct()
                .toList();

        model.addAttribute("categories", activeCategories);
        model.addAttribute("uniqueCategories", uniqueCategories);
    }


    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Admin - Ecommerce Shopping Cart");
        return "admin/index";
    }

    @GetMapping("/category")
    public String category(Model model, @RequestParam(name="pageNo", defaultValue = "0") Integer pageNo, @RequestParam(name="pageSize", defaultValue = "5") Integer pageSize) {
        model.addAttribute("title", "Catagories - Ecommerce Shopping Cart");

        Page<SubCategory> page=categoryService.getAllSubCategoryPagination(pageNo, pageSize);
        List<SubCategory> subCategories=page.getContent();

        AtomicInteger sno = new AtomicInteger(pageNo * pageSize);
        page.forEach(sub -> sub.setSno(sno.incrementAndGet()));

        model.addAttribute("subcategories", subCategories);
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", page.getSize());
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/category";
    }

    @PostMapping("/saveCategory")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveCategory(@RequestParam("categoryName") String categoryName, @RequestParam("subcategoryName") String subcategoryName,
                                                            @RequestParam("description") String description, @RequestParam("isActive") Boolean isActive,
                                                            @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1️⃣ Validate file
            if (file == null || file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Category image is required!");
                return ResponseEntity.ok(response);
            }

            // 2️⃣ Check if category exists
            Category category = categoryRepository.findByCategoryName(categoryName);
            if (category == null) {
                category = new Category();
                category.setCategoryName(categoryName);
            }

            // 3️⃣ Check if subcategory exists under this category
            boolean subExists = category.getSubCategories() != null &&
                    category.getSubCategories().stream()
                            .anyMatch(sc -> sc.getSubcategoryName().equalsIgnoreCase(subcategoryName));
            if(subExists) {
                response.put("status", "error");
                response.put("message", "Subcategory already exists under this category!");
                return ResponseEntity.ok(response);
            }

            // 4️⃣ Create subcategory
            SubCategory subCategory = new SubCategory();
            subCategory.setSubcategoryName(subcategoryName);
            subCategory.setDescription(description);
            subCategory.setIsActive(isActive);
            subCategory.setCategory(category);


            // 4️⃣ Handle category image
            String imageName = file.getOriginalFilename();
            subCategory.setImageName(imageName);

            // Save profile image if uploaded
            if (file != null && !file.isEmpty()) {
                File saveDir =  new ClassPathResource("static/img").getFile();

                // Ensure folder exists
                if (!saveDir.exists()) {
                    saveDir.mkdirs();
                }

                Path path = Paths.get(saveDir.getAbsolutePath(), "category_img", file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image is Uploaded");
            }

            // 5️⃣ Add subcategory to category and save
            if (category.getSubCategories() != null) {
                category.getSubCategories().add(subCategory);
            }
            else {
                category.setSubCategories(List.of(subCategory));
            }

            Category savedCategory = categoryService.saveCategory(category);
            if(savedCategory == null) {
                response.put("status", "error");
                response.put("message", "Failed to save category due to server error!");
            }
            else{

                response.put("status", "success");
                response.put("message", "Category added successfully!");
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Something went wrong! " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }


    @PostMapping("/updateCategory")
    @ResponseBody
    public Map<String, Object> updateCategory(@RequestParam("categoryId") Long categoryId, @RequestParam("subcategoryId") Long subcategoryId,
                                              @RequestParam("subcategoryName") String subcategoryName, @RequestParam("description") String description,
                                              @RequestParam("isActive") Boolean isActive, @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 1️⃣ Fetch existing category
            Category category = categoryService.getCategoryById(categoryId);
            if (category == null) {
                response.put("status", "error");
                response.put("message", "Category not found!");
                return response;
            }

            // 2️⃣ Find the subcategory
            SubCategory subCategory = category.getSubCategories().stream()
                    .filter(sc -> sc.getId().equals(subcategoryId))
                    .findFirst()
                    .orElse(null);

            if (subCategory == null) {
                response.put("status", "error");
                response.put("message", "Subcategory not found!");
                return response;
            }


            // 2️⃣ Handle image
            if (!file.isEmpty()) {
                // delete old photo
                String oldImageName = subCategory.getImageName();
                if(oldImageName != null && !"default.jpg".equals(oldImageName)) {
                    File saveFile = new ClassPathResource("static/img").getFile();
                    Path oldImagePath = Paths.get(saveFile.getAbsolutePath(), "category_img", oldImageName);
                    Files.deleteIfExists(oldImagePath);
                    System.out.println("Deleted old image file: " + oldImageName);
                }

                // Save new photo
                String newImageName = file.getOriginalFilename();
                File saveFile = new ClassPathResource("static/img").getFile();
                Path newImagePath = Paths.get(saveFile.getAbsolutePath(), "category_img", newImageName);
                Files.copy(file.getInputStream(), newImagePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Uploaded new image: " + newImageName);

                subCategory.setImageName(newImageName);
            }

            // 4️⃣ Update subcategory fields
            subCategory.setSubcategoryName(subcategoryName);
            subCategory.setDescription(description);
            subCategory.setIsActive(isActive);

            // 5️⃣ Save category (cascades subcategories)
            categoryService.saveCategory(category);

            // 6️⃣ Optionally deactivate products if subcategory is inactive
            if (!isActive) {
                productService.deactivateProductsByCategory(category.getCategoryName(), subcategoryName, 0, 5);
            }

            response.put("status", "success");
            response.put("message", "Category updated successfully!");
        }
        catch(Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Something went wrong! " + e.getMessage());
        }

        return response;
    }


    @DeleteMapping("/deleteSubCategory/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        System.out.println(id);

        try {
            // 1️⃣ Fetch category
            SubCategory subcategory = categoryService.getSubcategoryById(id);
            if(subcategory == null) {
                response.put("status", "error");
                response.put("message", "Subcategory not found!");
                return ResponseEntity.ok(response);
            }

            // Check if it is the only subcategory in the parent category
            Category parentCategory = subcategory.getCategory();
            if(parentCategory != null && parentCategory.getSubCategories().size() <= 1) {
                response.put("status", "warning");
                response.put("message", "Cannot delete the only subcategory of a category. You may deactivate it instead.");
                return ResponseEntity.ok(response);
            }

            // Delete subcategory image
            String subImage = subcategory.getImageName();
            if (subImage != null && !subImage.isEmpty() && !"default.jpg".equals(subImage)) {
                try {
                    File saveFile = new ClassPathResource("static/img").getFile();
                    Path imagePath = Paths.get(saveFile.getAbsolutePath(), "category_img", subImage);
                    Files.deleteIfExists(imagePath);
                }
                catch (IOException ioEx) {
                    System.err.println("Failed to delete subcategory image: " + ioEx.getMessage());
                }
            }

            // 3️⃣ Delete category (cascade deletes subcategories)
            boolean deletedFromDb = categoryService.deleteSubCategory(id);
            if (deletedFromDb) {
                response.put("status", "success");
                response.put("message", "Subcategory deleted successfully!");
            }
            else {
                response.put("status", "error");
                response.put("message", "Failed to delete subcategory!");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Something went wrong! " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }


    @GetMapping("/brands")
    public String brands(Model model, @RequestParam(name="pageNo", defaultValue = "0") Integer pageNo, @RequestParam(name="pageSize", defaultValue = "5") Integer pageSize) {
        model.addAttribute("brands", categoryService.getAllBrands());
        model.addAttribute("title", "Manage Brands");

        return "admin/brands";
    }

    @PostMapping("/brands/add")
    public ResponseEntity<Map<String, Object>> addBrand(@RequestParam("subcategoryId") Long subCategoryId,
                                                        @RequestParam("name") String name, @RequestParam("description") String description,
                                                        @RequestParam("isActive") Boolean isActive, @RequestParam(value = "file", required = false) MultipartFile file) {

        Map<String, Object> response = new HashMap<>();
        try {
            // ✅ Fetch subcategory safely
            SubCategory subCategory = subCategoryRepository.findById(subCategoryId)
                    .orElseThrow(() -> new RuntimeException("SubCategory not found"));

            // ✅ Create brand
            Brand brand = new Brand();
            brand.setName(name);
            brand.setDescription(description);
            brand.setIsActive(isActive);
            brand.setSubCategory(subCategory);

            // ✅ Handle logo upload
            if (file != null && !file.isEmpty()) {
                File saveDir =  new ClassPathResource("static/img").getFile();

                // Ensure folder exists
                if (!saveDir.exists()) {
                    saveDir.mkdirs();
                }

                String newImageName = file.getOriginalFilename();
                File saveFile = new ClassPathResource("static/img").getFile();
                Path newImagePath = Paths.get(saveFile.getAbsolutePath(), "brand_img", newImageName);
                Files.copy(file.getInputStream(), newImagePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Uploaded new image: " + newImageName);
                brand.setLogo(newImageName);
            }

            // ✅ Save brand
            brandRepository.save(brand);
            response.put("status", "success");
            response.put("message", "Brand added successfully!");

        }
        catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Something went wrong! " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/brands/update")
    public ResponseEntity<Map<String, Object>> updateBrand(@RequestParam("brandId") Long brandId, @RequestParam("subcategoryId") Long subCategoryId,
                                                           @RequestParam("name") String name, @RequestParam("description") String description,
                                                           @RequestParam("isActive") Boolean isActive, @RequestParam(value = "file", required = false) MultipartFile file) {

        Map<String, Object> response = new HashMap<>();
        try {
            Brand brand = brandRepository.findById(brandId)
                    .orElseThrow(() -> new RuntimeException("Brand not found"));

            SubCategory subCategory = subCategoryRepository.findById(subCategoryId)
                    .orElseThrow(() -> new RuntimeException("SubCategory not found"));

            brand.setName(name);
            brand.setDescription(description);
            brand.setIsActive(isActive);
            brand.setSubCategory(subCategory);

            if (file != null && !file.isEmpty()) {
                File staticDir = new ClassPathResource("static/img").getFile();
                File brandDir = new File(staticDir, "brand_img");
                if (!brandDir.exists())
                    brandDir.mkdirs();

                String fileName = file.getOriginalFilename();
                Path newPath = Paths.get(brandDir.getAbsolutePath(), fileName);
                Files.copy(file.getInputStream(), newPath, StandardCopyOption.REPLACE_EXISTING);

                brand.setLogo(fileName);
            }

            brandRepository.save(brand);
            response.put("status", "success");
            response.put("message", "Brand updated successfully!");
        }
        catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error updating brand: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/brands/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteBrand(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Brand> optionalBrand = brandRepository.findById(id);
            if (optionalBrand.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Brand not found!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Brand brand = optionalBrand.get();

            // ✅ Delete associated logo if exists
            if (brand.getLogo() != null && !brand.getLogo().isEmpty()) {
                File staticDir = new ClassPathResource("static/img/brand_img").getFile();
                File logoFile = new File(staticDir, brand.getLogo());
                if (logoFile.exists()) {
                    logoFile.delete();
                }
            }

            brandRepository.delete(brand);

            response.put("status", "success");
            response.put("message", "Brand deleted successfully!");
        }
        catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error deleting brand: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }




    @GetMapping("/addproduct")
    public String addProduct(Model model) {
        // Fetch categories with at least one active subcategory
        List<Category> activeCategories = categoryService.getAllActiveCategory();

        // Filter subcategories inside each category to only keep active ones
        activeCategories.forEach(cat ->
                cat.setSubCategories(
                        cat.getSubCategories().stream()
                                .filter(sub -> Boolean.TRUE.equals(sub.getIsActive()))
                                .toList()
                )
        );

        activeCategories.forEach(c -> System.out.println(c.getCategoryName() + " -> " + c.getSubCategories().size()));

        model.addAttribute("categories",activeCategories);
        model.addAttribute("title", "Add Product - Ecommerce Shopping Cart");

        return "admin/add_product";
    }

    @GetMapping("/subcategories/{categoryId}")
    @ResponseBody
    public List<SubCategoryDTO> getSubcategories(@PathVariable Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);

        return category.getSubCategories()
                .stream()
                .filter(SubCategory::getIsActive)
                .map(sub -> new SubCategoryDTO(sub.getId(), sub.getSubcategoryName()))
                .toList();
    }

    @GetMapping("/brands/{subCategoryId}")
    @ResponseBody
    public List<BrandDTO> getBrandsBySubcategory(@PathVariable Long subCategoryId) {
        return brandRepository.findBySubCategory_Id(subCategoryId)
                .stream()
                .filter(Brand::getIsActive) // show only active brands
                .map(brand -> new BrandDTO(
                        brand.getId(),
                        brand.getName(),
                        brand.getProducts().stream().count()
                ))
                .toList();
    }



    @GetMapping("/products")
    public String viewProducts(Model model, @RequestParam(name="pageNo", defaultValue = "0") Integer pageNo, @RequestParam(name="pageSize", defaultValue = "5") Integer pageSize) {

        Page<Product> page = productService.getAllProduct(pageNo, pageSize);
        List<Product> allProducts=page.getContent();

        System.out.println(allProducts.size());

        // Map sizes for Thymeleaf
        allProducts.forEach(p -> {
            Map<String, String> colorHexMap = p.getColors().stream()
                    .collect(Collectors.toMap(ProductColor::getColorName, ProductColor::getHexCode));
            p.setColorHexMap(colorHexMap);

            Map<String, Integer> sizeStockMap = p.getSizes().stream()
                    .collect(Collectors.toMap(ProductSize::getSize, ProductSize::getStock));
            // Store it in a transient field in Product
            p.setSizeStockMap(sizeStockMap);
        });

        List<Category> categories = categoryService.getAllActiveCategory();
        model.addAttribute("categories", categories);


        allProducts.forEach(p -> {
            System.out.println("Product ID: " + p.getId());
            System.out.println("Name: " + p.getName());
            System.out.println("Category: " + p.getCategory());
            System.out.println("Subcategory: " + p.getSubcategory());
            System.out.println("Colors: " + p.getColors().stream()
                    .map(ProductColor::getColorName)
                    .toList());
            System.out.println("Sizes: " + p.getSizes().stream()
                    .map(ProductSize::getSize)
                    .toList());
            System.out.println("----------------------------");
        });

        model.addAttribute("title", "Products - Ecommerce Shopping Cart");
        model.addAttribute("products",allProducts);
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", page.getSize());
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/view_product";
    }


    @PostMapping("/saveProduct")
    @ResponseBody
    public Map<String, String> saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file,
                                           @RequestParam(name="categoryId") Long categoryId, @RequestParam(name="subcategory", required=false) Long subcategoryId,
                                           @RequestParam(name="colorNames", required=false) List<String> colorNames, @RequestParam(name="hexCodes", required=false) List<String> hexCodes,
                                           @RequestParam(name="sizeNames", required=false) List<String> sizeNames, @RequestParam(name="sizeStocks", required=false) List<Integer> sizeStocks) {
        Map<String, String> response = new HashMap<>();
        try {
            // Category
            Category category = categoryService.getCategoryById(categoryId);
            SubCategory subcategory = categoryService.getSubcategoryById(subcategoryId);
            if (category == null) {
                response.put("status", "error");
                response.put("message", "Invalid category selected!");
                return response;
            }
            product.setCategory(category.getCategoryName());
            product.setSubcategory(subcategory.getSubcategoryName());

            // Image
            String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
            product.setImage(imageName);

            // Discount
            if (product.getDiscount() > 0)
                product.setDiscountPrice(product.getPrice() * (100 - product.getDiscount()) / 100);
            else {
                product.setDiscount(0);
                product.setDiscountPrice(product.getPrice());
            }

            // Colors
            if (colorNames != null && hexCodes != null && colorNames.size() == hexCodes.size()) {
                List<ProductColor> colors = new ArrayList<>();
                for (int i = 0; i < colorNames.size(); i++) {
                    ProductColor pc = new ProductColor();
                    pc.setColorName(colorNames.get(i));
                    pc.setHexCode(hexCodes.get(i));
                    pc.setProduct(product);
                    colors.add(pc);
                }
                product.setColors(colors);
            }

            // Sizes
            if (sizeNames != null && sizeStocks != null && sizeNames.size() == sizeStocks.size()) {
                List<ProductSize> sizes = new ArrayList<>();
                for (int i = 0; i < sizeNames.size(); i++) {
                    ProductSize ps = new ProductSize();
                    ps.setSize(sizeNames.get(i));
                    ps.setStock(sizeStocks.get(i));
                    ps.setProduct(product);
                    sizes.add(ps);
                }
                product.setSizes(sizes);
                product.updateStockFromSizes();
            }

            // Save product
            Product savedProduct = productService.saveProduct(product);
            if (savedProduct == null) {
                response.put("status", "error");
                response.put("message", "Internal server error! Product not saved.");
                return response;
            }

            // Save image file
            File saveFile = new ClassPathResource("static/img").getFile();
            File saveDir = new File(saveFile, "product_img");
            if (!saveDir.exists()) saveDir.mkdirs();

            Path path = Paths.get(saveDir.getAbsolutePath(), imageName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            response.put("status", "success");
            response.put("message", "Product saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Something went wrong!");
        }

        return response;
    }


    @GetMapping("/deleteProduct/{id}")
    @ResponseBody
    public Map<String, String> deleteProductAjax(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();

        try {
            // 1. Get product from DB
            Product oldProduct = productService.getProductById(id);
            if (oldProduct == null) {
                response.put("status", "error");
                response.put("message", "Product not found!");
                return response;
            }

            String imageName = oldProduct.getImage();

            // 2. Delete product from DB
            boolean deletedFromDb = productService.deleteProduct(id);

            if (deletedFromDb) {
                // 3. Delete image file from disk (if not default)
                if (imageName != null && !imageName.isEmpty() && !"default.jpg".equals(imageName)) {
                    File saveFile = new ClassPathResource("static/img").getFile();
                    Path imagePath = Paths.get(saveFile.getAbsolutePath(), "product_img", imageName);

                    try {
                        if (Files.deleteIfExists(imagePath)) {
                            System.out.println("Deleted image file: " + imageName);
                        }
                        else {
                            System.out.println("Image file not found: " + imageName);
                        }
                    }
                    catch (IOException ioEx) {
                        System.err.println("Failed to delete image: " + ioEx.getMessage());
                    }
                }

                response.put("status", "success");
                response.put("message", "Product deleted successfully!");
            }
            else {
                response.put("status", "error");
                response.put("message", "Failed to delete product from database!");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Something went wrong!");
        }

        return response;
    }



    @PostMapping("/updateProduct")
    @ResponseBody
    public Map<String, String> updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "colorNames", required = false) List<String> colorNames, @RequestParam(value = "hexCodes", required = false) List<String> hexCodes,
                                             @RequestParam(value = "sizeNames", required = false) List<String> sizeNames, @RequestParam(value = "sizeStocks", required = false) List<Integer> sizeStocks) {

        Map<String, String> response = new HashMap<>();
        try {
            Product oldProduct = productService.getProductById(product.getId());
            if (oldProduct == null) {
                response.put("status", "error");
                response.put("message", "Product not found!");
                return response;
            }
            if (product.getDiscount() < 0 || product.getDiscount() > 100) {
                response.put("status", "error");
                response.put("message", "Invalid discount! Must be between 0 and 100.");
                return response;
            }

            String imageName = oldProduct.getImage(); // keep old image by default
            if (!file.isEmpty()) {
                imageName = file.getOriginalFilename();
                File saveDir = new ClassPathResource("static/img/product_img").getFile();
                Path newImagePath = Paths.get(saveDir.getAbsolutePath(), imageName);

                // Delete old image only if it's different
                if (oldProduct.getImage() != null && !oldProduct.getImage().equals(imageName)) {
                    Path oldImagePath = Paths.get(saveDir.getAbsolutePath(), oldProduct.getImage());
                    Files.deleteIfExists(oldImagePath);
                }

                Files.copy(file.getInputStream(), newImagePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Uploaded new image: " + imageName);
            }

            // Update product fields
            // oldProduct.setName(product.getName());
            oldProduct.setName(product.getName());
            oldProduct.setTitle(product.getTitle());
            oldProduct.setDescription(product.getDescription());
            oldProduct.setPrice(product.getPrice());
            oldProduct.setStock(product.getStock() != null ? product.getStock() : 0);
            oldProduct.setCategory(product.getCategory());
            oldProduct.setSubcategory(product.getSubcategory());
            oldProduct.setIsActive(product.getIsActive());
            oldProduct.setImage(imageName);
            oldProduct.setDiscount(product.getDiscount());
            // Calculate discount price
            Double discountPrice = product.getPrice() - (product.getPrice() * product.getDiscount() / 100.0);
            oldProduct.setDiscountPrice(discountPrice);


            List<ProductColor> existingColors = oldProduct.getColors();
            Set<String> selectedColorSet = (colorNames != null)
                    ? colorNames.stream().map(String::toUpperCase).collect(Collectors.toSet())
                    : new HashSet<>();

            // Remove unchecked colors
            Iterator<ProductColor> colorIterator = existingColors.iterator();
            while (colorIterator.hasNext()) {
                ProductColor pc = colorIterator.next();
                if(!selectedColorSet.contains(pc.getColorName().toUpperCase())) {
                    colorIterator.remove(); // orphanRemoval = true will delete it from DB
                }
            }

            // Add new colors
            if(colorNames != null && hexCodes != null) {
                for(int i = 0; i < colorNames.size(); i++) {
                    String name = colorNames.get(i);
                    String hex = hexCodes.get(i);
                    boolean exists = existingColors.stream().anyMatch(c -> c.getColorName().equalsIgnoreCase(name));
                    if(!exists) {
                        ProductColor newColor = new ProductColor();
                        newColor.setColorName(name);
                        newColor.setHexCode(hex);
                        newColor.setProduct(oldProduct);
                        existingColors.add(newColor);
                    }
                }
            }

            oldProduct.setColors(existingColors);
            System.out.println("Colors after update:");
            existingColors.forEach(c -> System.out.println("Color: " + c.getColorName() + ", Hex: " + c.getHexCode()));

            // --- Update sizes only for Clothing ---
            if ("Clothing".equalsIgnoreCase(oldProduct.getCategory())) {
                List<ProductSize> existingSizes = oldProduct.getSizes();
                // Convert selected size names to a Set for quick lookup
                Set<String> selectedSizes = (sizeNames != null) ? new HashSet<>(sizeNames) : new HashSet<>();
                // --- Remove sizes that are no longer selected ---
                existingSizes.removeIf(size -> !selectedSizes.contains(size.getSize()));
                // --- Update or add new sizes ---
                if(sizeNames != null && sizeStocks != null && sizeNames.size() == sizeStocks.size()) {
                    for(int i = 0; i < sizeNames.size(); i++) {
                        String sizeName = sizeNames.get(i);
                        Integer stockValue = sizeStocks.get(i);

                        // Check if this size already exists
                        ProductSize existing = existingSizes.stream().filter(s -> s.getSize().equalsIgnoreCase(sizeName)).findFirst().orElse(null);
                        if(existing != null) {
                            existing.setStock(stockValue);
                        }
                        else {
                            ProductSize newSize = new ProductSize();
                            newSize.setSize(sizeName);
                            newSize.setStock(stockValue);
                            newSize.setProduct(oldProduct);
                            existingSizes.add(newSize);
                        }
                    }
                }

                oldProduct.setSizes(existingSizes);
                oldProduct.updateStockFromSizes(); // updates total stock automatically
            }
            else {
                // Not clothing → remove all sizes
                oldProduct.getSizes().clear();
                oldProduct.setStock(product.getStock()); // Use single stock input
            }

            // Save updated product
            Product updatedProduct = productService.saveProduct(oldProduct);
            if (ObjectUtils.isEmpty(updatedProduct)) {
                response.put("status", "error");
                response.put("message", "Product not saved! Internal server error.");
            }
            else {
                response.put("status", "success");
                response.put("message", "Product updated successfully!");
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Something went wrong!");
        }

        return response;
    }



    @GetMapping("/users")
    public String getAllUsers(Model model){
        List<User> users = userService.getAllUsers("ROLE_USER");
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/updateStatus")
    public String updateUserAccountStatus(@RequestParam Long id, @RequestParam Boolean status, HttpSession session) {
        try {
            Boolean flag = userService.updateAccountStatus(id, status);
            if (flag) {
                session.setAttribute("userMessage", new Message("Account Status Updated!", "success"));
            }
            else {
                session.setAttribute("userMessage", new Message("Account Status is not Updated!", "danger"));
            }
        }
        catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            session.setAttribute("userMessage", new Message("Something went wrong!", "danger"));
            e.printStackTrace();
        }
        return "redirect:/admin/users";
    }



    @GetMapping("/orders")
    public String getAllOrders(Model model, @RequestParam(name="pageNo", defaultValue = "0") Integer pageNo, @RequestParam(name="pageSize", defaultValue = "5") Integer pageSize){

        Page<Order> page=orderService.getAllOrders(pageNo, pageSize);
        List<Order> allOrders=page.getContent();

        model.addAttribute("title", "All Orders - Ecommerce Shopping Cart");
        model.addAttribute("orders", allOrders);
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", page.getSize());
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/orders";
    }

    @GetMapping("/admin/orders/{id}")
    @ResponseBody
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        (long) i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getPrice()
                )).toList();

        OrderResponse response = new OrderResponse(
                order.getOrderId(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getPaymentStatus(),
                order.getPaymentType(),
                order.getPaymentId(),
                order.getOrderDate(),
                order.getOrderAddress(),
                items
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateOrder")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@RequestParam("orderId") Long orderId, @RequestParam("orderStatus") String orderStatus) {
        Map<String, Object> response = new HashMap<>();

        try {
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
            String oldStatus = order.getOrderStatus();
            order.setOrderStatus(orderStatus);
            orderRepository.save(order);
            System.out.println(orderStatus);

            // ✅ Send mail only for specific statuses
            if ("Received".equals(orderStatus) || "Out for Delivery".equals(orderStatus) ||
                    "Delivered".equals(orderStatus) || "Cancelled".equals(orderStatus)) {
                System.out.println(order);
                emailUtil.sendMailForOrder(order, orderStatus);
            }

            // Use this if you want invoice ONLY when delivered:
//            if ("Delivered".equals(orderStatus)) {
                String pdfPath = jasperReportUtil.generateOrderInvoice(order);
                System.out.println("Invoice Generated: " + pdfPath);
//            }

            // Set timeline steps using orderDate as base
            List<OrderStep> steps = new ArrayList<>();
            List<OrderStatus> allSteps = List.of(
                    OrderStatus.IN_PROGRESS,
                    OrderStatus.RECEIVED,
                    OrderStatus.PACKED,
                    OrderStatus.OUT_FOR_DELIVERY,
                    OrderStatus.DELIVERED
            );

            // Use orderDate as start time, fallback to now if null
            LocalDateTime baseTime = order.getOrderDate() != null
                    ? order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    : LocalDateTime.now().minusDays(1);

            int currentStepIndex = 0;
            for (int i = 0; i < allSteps.size(); i++) {
                OrderStatus stepStatus = allSteps.get(i);
                LocalDateTime stepDate = null;

                // Steps before or equal to current status are considered completed
                if (stepStatus.getName().equalsIgnoreCase(orderStatus) ||
                        i < allSteps.indexOf(OrderStatus.valueOf(orderStatus.toUpperCase().replace(" ", "_")))) {
                    stepDate = baseTime.plusHours(i * 2); // Each step +2 hours from orderDate
                    currentStepIndex = i;
                }

                steps.add(new OrderStep(stepStatus, stepDate));
            }

            order.setOrderSteps(steps);
            order.setCurrentStepIndex(currentStepIndex);

            // ✅ Build JSON success response
            response.put("success", true);
            response.put("message", "Order #" + order.getOrderId() + " updated from " + oldStatus + " to " + orderStatus);
            response.put("newStatus", orderStatus);

            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/colors")
    public String colors(Model model){
        model.addAttribute("title", "Manage Colors");
        return "admin/colors";
    }
}
