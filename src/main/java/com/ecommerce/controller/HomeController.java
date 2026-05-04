package com.ecommerce.controller;

import com.ecommerce.helper.Message;
import com.ecommerce.payload.dto.BrandDTO;
import com.ecommerce.model.Users.Products.Category;
import com.ecommerce.model.Users.Products.SubCategory;
import com.ecommerce.model.Users.Profile.User;
import com.ecommerce.model.Users.Products.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.service.CartService;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.UserService;
import com.ecommerce.util.Colors;
import com.ecommerce.util.EmailUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CartService cartService;


    @ModelAttribute
    public void getUserDetails(Model model, Principal principal) {
        if(principal == null) {
            model.addAttribute("User", null);
            model.addAttribute("countCart", 0);
        }
        else {
            User user = userService.getUserByEmail(principal.getName());
            model.addAttribute("User", user);

            Integer countCart = cartService.getCountCart(user.getId());
            model.addAttribute("countCart", countCart);
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



    @RequestMapping("/")
    public String index(Model model) {
        // Fetch all active categories (with subcategories)
        List<Category> allCategories = categoryService.getAllCategory();

        // Build category → subcategories map (only active subcategories)
        Map<String, List<SubCategory>> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(
                        Category::getCategoryName,
                        cat -> cat.getSubCategories().stream()
                                .filter(SubCategory::getIsActive) // only active subcategories
                                .toList(),
                        (a, b) -> b, // merge function (not needed here)
                        LinkedHashMap::new // preserve insertion order
                ));


        model.addAttribute("categoryMap", categoryMap);
        model.addAttribute("title", "Home - Ecommerce Shopping Cart");

        return "index";
    }

    @GetMapping("/signin")
    public String login(Model model) {

        model.addAttribute("title", "Login - Ecommerce Shopping Cart");

        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        String[] countries = Arrays.stream(Locale.getISOCountries())
                .map(code -> new Locale("", code).getDisplayCountry())
                .sorted()
                .toArray(String[]::new);

        model.addAttribute("countries", countries);
        model.addAttribute("title", "Register - Ecommerce Shopping Cart");

        return "register";
    }

    @GetMapping("/products")
    public String products(Model model, @RequestParam(value = "category", defaultValue = "") String categoryName,  @RequestParam(value = "subcategory", defaultValue = "") String subcategoryName,
                           @RequestParam(value = "colors", required = false) List<String> selectedColors,
                           @RequestParam(value = "minPrice", required = false) Double minPrice, @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                           @RequestParam(name="pageNo", defaultValue = "0") Integer pageNo, @RequestParam(name="pageSize", defaultValue = "5") Integer pageSize) {
        // Fetch all categories with at least one active subcategory
        List<Category> activeCategories = categoryService.getAllActiveCategory();

        // Filter subcategories to only active ones
        activeCategories.forEach(cat ->
                cat.setSubCategories(
                        cat.getSubCategories().stream()
                                .filter(SubCategory::getIsActive)
                                .toList()
                )
        );

        if (selectedColors != null && !selectedColors.isEmpty()) {
            for (String color : selectedColors) {
                System.out.println("Selected color: " + color);
            }
        }
        else {
            System.out.println("No colors selected");
        }

        // Fetch paginated products (filtering implemented in productService)
//        Page<Product> page = productService.getAllActiveProducts(categoryName, subcategoryName, pageNo, pageSize);
        Page<Product> page;
        if ((minPrice != null || maxPrice != null) && (!categoryName.isEmpty() || !subcategoryName.isEmpty() || (selectedColors != null && !selectedColors.isEmpty()))) {
            // Filter by price + category/subcategory + colors
            page = productService.getProductsByFilters(categoryName, subcategoryName, selectedColors, minPrice, maxPrice, pageNo, pageSize);
        }
        else if (selectedColors != null && !selectedColors.isEmpty() && (!categoryName.isEmpty() || !subcategoryName.isEmpty())) {
            // Filter by category/subcategory + colors
            page = productService.getProductsByCategorySubcategoryAndColors(categoryName, subcategoryName, selectedColors, pageNo, pageSize);
        }
        else if (selectedColors != null && !selectedColors.isEmpty()) {
            // Filter only by colors
            page = productService.getProductsByColors(selectedColors, pageNo, pageSize);
        }
        else if (!categoryName.isEmpty() || !subcategoryName.isEmpty()) {
            // Filter only by category/subcategory
            page = productService.getAllActiveProducts(categoryName, subcategoryName, pageNo, pageSize);
        }
        else if (minPrice != null || maxPrice != null) {
            // Filter only by price
            page = productService.getProductsByPriceRange(minPrice, maxPrice, pageNo, pageSize);
        }
        else {
            // No filters
            page = productService.getAllActiveProducts("", "", pageNo, pageSize);
        }

        List<Product> products = page.getContent();
        Colors[] colors = Colors.values();
        List<BrandDTO> brands = categoryService.getAllBrands();

        model.addAttribute("title", "Products - Ecommerce Shopping Cart");
        model.addAttribute("Categories", activeCategories);
        model.addAttribute("Products", products);
        model.addAttribute("colors", colors);
        model.addAttribute("selectedColors", selectedColors);
        model.addAttribute("brands", brands);

        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", page.getSize());
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "product";
    }



    @GetMapping("/product/{id}")
    public String product(@PathVariable Long id,  Model model) {

        model.addAttribute("title", "Product - Ecommerce Shopping Cart");

        Product product=productService.getProductWithRating(id);
        model.addAttribute("product", product);
        model.addAttribute("colors", product.getColors());

        if ("Clothing".equalsIgnoreCase(product.getCategory())) {
            model.addAttribute("sizes", product.getSizes());
        }

        return "view_product";
    }

    @GetMapping("/about")
    public String about(Model model) {

        model.addAttribute("title", "About - Ecommerce Shopping Cart");

        return "about";
    }


    @PostMapping("/saveUser")
    @ResponseBody
    public Map<String, String> saveUser(Model model, @ModelAttribute User user, @RequestParam("profile_img") MultipartFile file, HttpSession session){
        Map<String, String> response = new HashMap<>();
        try{
            // Set image name (default if not uploaded)
            String imageName = (file != null && !file.isEmpty()) ? file.getOriginalFilename() : "default.jpg";
            user.setProfileImage(imageName);

            // Save user to DB
            User savedUser = userService.saveUser(user);
            if (ObjectUtils.isEmpty(savedUser)) {
                response.put("status", "error");
                response.put("message", "User not saved. Internal server error!");
                session.setAttribute("userMessage", new Message("Not saved!! Internal server error !!", "danger"));
                return response;
            }

            // Save profile image if uploaded
            if (file != null && !file.isEmpty()) {
                File saveDir =  new ClassPathResource("static/img").getFile();

                // Ensure folder exists
                if (!saveDir.exists()) {
                    saveDir.mkdirs();
                }

                Path path = Paths.get(saveDir.getAbsolutePath()+File.separator+"profile_img"+File.separator+file.getOriginalFilename());
                System.out.println(path);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Image is Uploaded");
            }

            response.put("status", "success");
            response.put("message", "Your details have been saved successfully!");
            session.setAttribute("userMessage", new Message("Your User Details are saved successfully !!", "success"));
            System.out.println("Data saved: " + user);
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("ERROR " + e.getMessage());
            response.put("status", "error");
            response.put("message", "Something went wrong!");
            session.setAttribute("userMessage", new Message("Something went wrong !!", "danger"));
        }

        return response;
    }


    @GetMapping("/forgot-password")
    public String forgotPassword(Model model){

        model.addAttribute("title", "Forgot Password - Ecommerce Shopping Cart");

        return "forgot";
    }

    @PostMapping("/forgot")
    public String processForgot(@RequestParam String email, HttpSession session, HttpServletRequest request){
        try {
            User user = userService.getUserByEmail(email);

            if (ObjectUtils.isEmpty(user)) {
                session.setAttribute("forgotMessage", new Message("Invalid Email!!", "danger"));
            }
            else{

                String resetToken=UUID.randomUUID().toString();
                userService.updateUserResetToken(email, resetToken);

                // Generate Url:  http://localhost:8080/reset-password?token=abdssddd
                String url=EmailUtil.generateUrl(request)+"/reset-password?token="+resetToken;

                Boolean sendMail=emailUtil.sendMail(url, email);
                if(sendMail){
                    session.setAttribute("forgotMessage", new Message("Please check your email, Password Reset link send!!", "warning"));
                }
                else{
                    session.setAttribute("forgotMessage", new Message("Something went wrong on server! Email not sent!! ", "danger"));

                }
            }
        }
        catch(Exception e) {
            System.out.println("ERROR " + e.getMessage());
            session.setAttribute("forgotMessage", new Message("Something went wrong !!", "danger"));
            e.printStackTrace();
        }

        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(Model model, @RequestParam String token, HttpSession session){

        model.addAttribute("title", "Reset Password - Ecommerce Shopping Cart");

        try {
            User userByToken = userService.getUserByToken(token);
            if (ObjectUtils.isEmpty(userByToken)) {
                model.addAttribute("error", "Your link is invalid or expired !!");
                return "error";
            }
        }
        catch(Exception e) {
            System.out.println("ERROR " + e.getMessage());
            session.setAttribute("resetMessage", new Message("Something went wrong !!", "danger"));
            e.printStackTrace();
        }

        model.addAttribute("token", token);
        return "reset";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(Model model, @RequestParam String token, @RequestParam String password, HttpSession session){

        try {
            User userByToken = userService.getUserByToken(token);
            if (ObjectUtils.isEmpty(userByToken)) {
                model.addAttribute("error", "Your link is invalid or expired !!");
                return "error";
            }
            else{
                userByToken.setPassword(passwordEncoder.encode(password));
                userByToken.setResetToken(null);
                userService.updateUser(userByToken);
                model.addAttribute("title", "Password Changed - Ecommerce Shopping Cart");
                model.addAttribute("success", "Password has been changed successfully!!");

                return "message";
            }

        }
        catch(Exception e) {
            System.out.println("ERROR " + e.getMessage());
            session.setAttribute("resetMessage", new Message("Something went wrong !!", "danger"));
            e.printStackTrace();
        }

        return "reset";
    }


    @GetMapping("/search")
    public String searchProduct(@RequestParam String keyword, Model model, @RequestParam(name="pageNo", defaultValue = "0") Integer pageNo, @RequestParam(name="pageSize", defaultValue = "5") Integer pageSize){
        Page<Product> page=productService.searchByCategoryOrSubcategory(keyword, pageNo, pageSize);
        List<Product> searchProduct=page.getContent();
        // Add categories & unique categories like in /products
        List<Category> activeCategories = categoryRepository.findDistinctBySubCategoriesIsActiveTrue();

        // 3️⃣ Extract unique category names for filters/dropdowns
        List<String> uniqueCategories = activeCategories.stream()
                .map(Category::getCategoryName)
                .distinct()
                .toList();

        model.addAttribute("Products", searchProduct);
        model.addAttribute("Categories", activeCategories);
        model.addAttribute("uniqueCategories", uniqueCategories);
        model.addAttribute("title", "Search Results for: " + keyword);
        model.addAttribute("keyword", keyword); // ✅ useful to pre-fill search box

        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", page.getSize());
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "product";
    }



}
