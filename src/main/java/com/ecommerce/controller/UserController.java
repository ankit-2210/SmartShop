package com.ecommerce.controller;

import com.ecommerce.helper.Message;
import com.ecommerce.model.Orders.Order;
import com.ecommerce.payload.response.Orders.OrderItemResponse;
import com.ecommerce.payload.request.Orders.OrderRequest;
import com.ecommerce.payload.response.Orders.OrderResponse;
import com.ecommerce.model.Reviews.Review;
import com.ecommerce.payload.request.Reviews.ReviewRequest;
import com.ecommerce.payload.request.Reviews.ReviewUpdateRequest;
import com.ecommerce.payload.request.Reviews.SingleReview;
import com.ecommerce.model.Users.Profile.Address;
import com.ecommerce.model.Users.Cart.Cart;
import com.ecommerce.model.Users.Cart.PaymentCard;
import com.ecommerce.model.Users.Products.Category;
import com.ecommerce.model.Users.Profile.User;
import com.ecommerce.model.Users.Wishlist.Wishlist;
import com.ecommerce.repository.*;
import com.ecommerce.service.*;
import com.ecommerce.util.EmailUtil;
import com.ecommerce.util.OrderStatus;
import com.ecommerce.util.OrderStep;
import com.razorpay.RazorpayClient;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private PaymentCardService paymentCardService;

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailUtil emailUtil;

    @ModelAttribute
    public void getUserDetails(Model model, Principal principal) {
        if(principal == null) {
            model.addAttribute("User", null);
        }
        else {
            User user = userService.getUserByEmail(principal.getName());
            model.addAttribute("User", user);

            Integer countCart = cartService.getCountCart(user.getId());
            model.addAttribute("countCart", countCart);

            List<Wishlist> wishlistsItems = wishlistService.getWishlistByUser((long) user.getId());
            model.addAttribute("wishlistCount", wishlistsItems.size());

            long ordersCount = orderService.countOrdersByUserId(user.getId());
            model.addAttribute("ordersCount", ordersCount);


        }

        List<Category> activeCategories = categoryRepository.findDistinctBySubCategoriesIsActiveTrue();
        List<String> uniqueCategories = activeCategories.stream()
                .map(Category::getCategoryName)
                .distinct()
                .toList();

        model.addAttribute("categories", activeCategories);
        model.addAttribute("uniqueCategories", uniqueCategories);
    }


    @GetMapping("/")
    public String home(Model model){

        model.addAttribute("title", "User Home - Ecommerce Shpoping Cart");

        return "user/home";
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session, Principal principal) {
        User user = getLoggedInUserDetails(principal);

        // (Optional) ensure we always fetch the fresh data from DB
        if (user != null) {
            user = userRepository.findById(user.getId()).orElse(user);
            session.setAttribute("loggedInUser", user); // refresh session user
        }

        model.addAttribute("User", user);
        // Always reload states for dropdown
        model.addAttribute("states", List.of("Delhi", "Mumbai", "Kolkata", "Chennai", "Bangalore", "Hyderabad"));

        model.addAttribute("title", "My Profile Page");
        model.addAttribute("page", "profile");

        return "user/profile"; // Thymeleaf template
    }


    @GetMapping("/settings")
    public String settings(Model model, HttpSession session, Principal principal) {
        User user = getLoggedInUserDetails(principal);
        System.out.println(user);

        // (Optional) ensure we always fetch the fresh data from DB
        if (user != null) {
            user = userRepository.findById(user.getId()).orElse(user);
            session.setAttribute("loggedInUser", user); // refresh session user
        }

        model.addAttribute("User", user);
        // Always reload states for dropdown
//        model.addAttribute("states", List.of("Delhi", "Mumbai", "Kolkata", "Chennai", "Bangalore", "Hyderabad"));
        model.addAttribute("title", "Settings Page");
        model.addAttribute("page", "settings");

        return "user/accountsettings"; // Thymeleaf template
    }


    @PostMapping("/updateProfile")
    @ResponseBody
    public Map<String, Object> updateUserProfile(@ModelAttribute User user, @RequestParam("file") MultipartFile file, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            User dbuser = userRepository.findById(user.getId()).orElse(null);
            if (dbuser == null) {
                session.setAttribute("userProfileMessage", new Message("User not found!", "danger"));
                response.put("success", false);
                response.put("message", "User not found!");
                return response;
            }

            String imageName = file.isEmpty() ? dbuser.getProfileImage() : file.getOriginalFilename();
            // If new file is uploaded
            if (!file.isEmpty()) {
                // Delete old photo if it's not the default one
                if (imageName != null && !imageName.isEmpty() && !"default.jpg".equals(dbuser.getProfileImage())) {
                    File saveFile = new ClassPathResource("static/img").getFile();
                    Path oldImagePath = Paths.get(saveFile.getAbsolutePath(), "profile_img", dbuser.getProfileImage());
                    Files.deleteIfExists(oldImagePath);
                    System.out.println("Deleted old image file: " + dbuser.getProfileImage());
                }

                // Save new photo
                File saveFile = new ClassPathResource("static/img").getFile();
                Path newImagePath = Paths.get(saveFile.getAbsolutePath(), "profile_img", imageName);
                Files.copy(file.getInputStream(), newImagePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Uploaded new image: " + imageName);
            }

            // === Update profile fields ===
            dbuser.setUsername(user.getUsername());
            dbuser.setEmail(user.getEmail());
            dbuser.setMobileNumber(user.getMobileNumber());
            dbuser.setAddress(user.getAddress());
            dbuser.setCountry(user.getCountry());
            dbuser.setCity(user.getCity());
            dbuser.setState(user.getState());
            dbuser.setPincode(user.getPincode());
            dbuser.setProfileImage(imageName);
            userRepository.save(dbuser);

            // ✅ Refresh session user
            session.setAttribute("loggedInUser", dbuser);
            // ✅ Add success message
            session.setAttribute("userProfileMessage", new Message("Your profile has been updated successfully!", "success"));
            response.put("success", true);
            response.put("message", "Your profile has been updated successfully!");
        }
        catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("userProfileMessage", new Message("Something went wrong while updating profile!", "danger"));
            response.put("success", false);
            response.put("message", "Something went wrong while updating profile!");
        }

        return response;
    }

    @GetMapping("/myorders")
    public String myOrders(Model model, Principal principal, @RequestParam(name="pageNo", defaultValue = "0") Integer pageNo,
                                                             @RequestParam(name="pageSize", defaultValue = "4") Integer pageSize,
                                                             @RequestParam(name = "status", defaultValue = "ALL") String status) {
        User user = getLoggedInUserDetails(principal);
        User dbUser = userRepository.findById(user.getId()).orElse(user);

        Page<Order> page;
        if("All".equalsIgnoreCase(status)){
            page=orderService.findOrdersByUserId(dbUser.getId(), pageNo, pageSize);
        }
        else{
            page=orderService.findOrdersByUserIdAndStatus(dbUser.getId(), status, pageNo, pageSize);
        }

        List<Order> orders=page.getContent();

        orders.forEach(order -> {
            int count = order.getItems().size();
            order.setDisplayItemCount(count + " " + (count == 1 ? "item" : "items"));

            // maark for review
            boolean allReviewed = order.getItems().stream().allMatch(item -> {
                boolean alreadyReviewed = reviewService
                        .getReviewsByProduct(item.getProduct().getId())
                        .stream()
                        .anyMatch(r -> Objects.equals(r.getUser().getId(), dbUser.getId()));

                item.setReviewed(alreadyReviewed);
                return alreadyReviewed;
            });

            order.setAllReviewed(allReviewed);


            // Define the timeline sequence
            List<OrderStatus> allSteps = List.of(
                    OrderStatus.IN_PROGRESS,
                    OrderStatus.RECEIVED,
                    OrderStatus.PACKED,
                    OrderStatus.OUT_FOR_DELIVERY,
                    OrderStatus.DELIVERED
            );

            List<OrderStep> steps = new ArrayList<>();

            // Base timeline start (orderDate or fallback)
            LocalDateTime baseTime = order.getOrderDate() != null
                    ? order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    : LocalDateTime.now().minusDays(1);

            // ✅ Use safe converter
            OrderStatus currentStatus = OrderStatus.fromDbValue(order.getOrderStatus());

            int currentStepIndex = 0;
            for (int i = 0; i < allSteps.size(); i++) {
                OrderStatus stepStatus = allSteps.get(i);
                LocalDateTime stepDate = null;

                // Completed steps: <= current status
                if (currentStatus != null && (stepStatus == currentStatus || i < allSteps.indexOf(currentStatus))) {
                    stepDate = baseTime.plusHours(i * 2);
                    currentStepIndex = i;
                }

                steps.add(new OrderStep(stepStatus, stepDate));
            }

            order.setOrderSteps(steps);
            order.setCurrentStepIndex(currentStepIndex);
        });

        model.addAttribute("orders", orders);
        model.addAttribute("title", "My Orders Page");
        model.addAttribute("page", "myorders");

        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", page.getSize());
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
        
        return "/user/myorders";
    }



    @GetMapping("/changePassword")
    public String changePassword(){

        return "/user/changePassword";
    }


    @PostMapping("/change-password")
    @ResponseBody
    public Map<String, String> changePasswordAjax(@RequestParam String newPassword,
                                                  @RequestParam String currPassword,
                                                  Principal principal,
                                                  HttpSession session){
        Map<String, String> response = new HashMap<>();
        try {
            User user = getLoggedInUserDetails(principal);
            if(passwordEncoder.matches(currPassword, user.getPassword())){
                user.setPassword(passwordEncoder.encode(newPassword));
                userService.updateUser(user);
                response.put("status", "success");
                response.put("message", "Password updated successfully!");
                session.setAttribute("changePasswordMessage", new Message("Password Updated Successfully!", "success"));
            }
            else {
                response.put("status", "error");
                response.put("message", "Current password is incorrect!");
                session.setAttribute("changePasswordMessage", new Message("Current Password is not matching!", "danger"));
            }
        }
        catch(Exception e) {
            response.put("status", "error");
            response.put("message", "Something went wrong!");
            System.out.println("ERROR: " + e.getMessage());
            session.setAttribute("changePasswordMessage", new Message("Something went wrong!", "danger"));
            e.printStackTrace();
        }
        return response;
    }


    @GetMapping("/wishlist")
    public String wishlist(Model model, Principal principal){
        User user = getLoggedInUserDetails(principal);
        List<Wishlist> wishlistsItems = wishlistService.getWishlistByUser(user.getId());

        // attach average rating to each product
        for (Wishlist item : wishlistsItems) {
            Double avgRating = reviewRepository.findAverageRatingByProductId(item.getProduct().getId());
            item.getProduct().setAverageRating((double) (avgRating != null ? avgRating.intValue() : 0)); // you can store double too
        }

        model.addAttribute("title", "Wishlist Page");
        model.addAttribute("page", "wishlist");
        model.addAttribute("wishlistItems", wishlistsItems);
        model.addAttribute("wishlistCount", wishlistsItems.size());

        return "user/wishlist";
    }


    @GetMapping("/addresses")
    public String addresses(Model model, Principal principal){

        model.addAttribute("title", "Address Page");
        model.addAttribute("page", "addresses");

        // Get logged-in user by email/username
        User user = userService.getUserByEmail(principal.getName());

        // Fetch addresses for this user
        List<Address> addresses = userService.getAddressesByUser(user);

        model.addAttribute("addresses", addresses);

        return "user/addresses";
    }

    @PostMapping("/address/add")
    public ResponseEntity<String> addAddress(@RequestBody Address address, Principal principal) {
        // Get logged-in user
        User user = getLoggedInUserDetails(principal);
        User dbUser = userRepository.findById(user.getId()).orElse(user);

        address.setUser(dbUser);

        // If marked default, reset old defaults
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.updateDefaultAddress(user.getId());
        }

        addressRepository.save(address);
        return ResponseEntity.ok("Address saved successfully");
    }

    @DeleteMapping("/address/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteAddress(@PathVariable Long id, Principal principal){
        User user = getLoggedInUserDetails(principal);

        Address address = userService.getAddressById(id);
        if (address == null || address.getUser() == null || !Objects.equals(address.getUser().getId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized action!");
        }

        userService.deleteAddress(id);
        return ResponseEntity.ok("Address removed successfully!");
    }

    @GetMapping("/address/{id}")
    @ResponseBody
    public ResponseEntity<Address> getAddress(@PathVariable Long id, Principal principal) {
        User user = getLoggedInUserDetails(principal);
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (address.getUser().getId() != user.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(address);
    }

    @PutMapping("/address/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable("id") Long addressId,
                                           @RequestBody Address updatedAddress,
                                           Principal principal) {
        try {
            // Get logged-in user
            User user = getLoggedInUserDetails(principal);

            // Update address in DB
            Address address = userService.updateAddress(user.getId(), addressId, updatedAddress);

            return ResponseEntity.ok(address); // return updated address as JSON
        }
        catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @PutMapping("/address/{id}/default")
    @ResponseBody
    public ResponseEntity<String> makeDefault(@PathVariable("id") Long addressId, Principal principal) {
        User user = getLoggedInUserDetails(principal);
        User dbUser = userRepository.findById(user.getId()).orElse(user);

        userService.makeDefault(user.getId(), addressId);
        return ResponseEntity.ok("Default address updated successfully!");
    }


    @GetMapping("/address/list")
    public List<Address> getUserAddresses(Principal principal) {
        User user = getLoggedInUserDetails(principal);
        User dbUser = userRepository.findById(user.getId()).orElse(user);

        return addressRepository.findByUserId(dbUser.getId());
    }



    @PostMapping("/addReviews")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addReviews(@RequestBody ReviewRequest request, Principal principal) {
        User user = getLoggedInUserDetails(principal);
        
        System.out.println(request.getReviews());

        for (SingleReview dto : request.getReviews()) {
            reviewService.saveReview(
                    user.getId(),
                    (long) dto.getOrderId(),
                    (long) dto.getProductId(),
                    dto.getRating(),
                    dto.getComment()
            );
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Reviews submitted successfully!"
        ));
    }


    @PostMapping("/updateReview")
    @ResponseBody
    public Map<String, String> updateReview(@RequestBody ReviewUpdateRequest request, Principal principal) {
        Map<String, String> response = new HashMap<>();
        try {
            User user = getLoggedInUserDetails(principal);

            Review review = reviewRepository.findById((long) request.getReviewId())
                    .orElseThrow(() -> new RuntimeException("Review not found"));
            if(!Objects.equals(review.getUser().getId(), user.getId())) {
                response.put("status", "error");
                response.put("message", "Unauthorized to update this review");
                return response;
            }

            review.setRating(request.getRating());
            review.setComment(request.getComment());
            review.setUpdatedAt(LocalDateTime.now());

            reviewRepository.save(review);

            response.put("status", "success");
            response.put("message", "Review updated successfully!");
        }
        catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update review");
        }
        return response;
    }


    @GetMapping("/myreviews")
    public String myreviews(Model model, Principal principal){
        User user = getLoggedInUserDetails(principal);
        User dbUser = userRepository.findById(user.getId()).orElseThrow();

        List<Review> reviews = reviewService.getReviewsByUser(dbUser.getId());

        model.addAttribute("title", "Reviews Page");
        model.addAttribute("page", "reviews");
        model.addAttribute("reviews", reviews);

        return "user/myreviews";
    }





    @GetMapping("/wallet")
    public String wallet(Model model, Principal principal){
        User user = getLoggedInUserDetails(principal);
        User dbUser = userRepository.findById(user.getId()).orElseThrow();

        model.addAttribute("title", "Payment Methods Page");
        model.addAttribute("page", "wallet");

        List<PaymentCard> paymentCards = paymentCardService.getPaymentCardByUser(user);
        model.addAttribute("paymentCards", paymentCards);


        return "user/paymentmethods";
    }

    @PostMapping("/payment/add")
    public ResponseEntity<String> addPaymentCard(@RequestBody PaymentCard paymentCard, Principal principal) {
        // Get logged-in user
        User user = getLoggedInUserDetails(principal);
        User dbUser = userRepository.findById(user.getId()).orElse(user);

        PaymentCard saved=paymentCardService.saveCard(paymentCard, user);
        return ResponseEntity.ok("Card saved successfully");
    }


    @PutMapping("/paymentCard/{id}")
    public ResponseEntity<?> updatePaymentCard(@PathVariable("id") Long cardId,
                                           @RequestBody PaymentCard paymentCard,
                                           Principal principal) {
        try {
            // Get logged-in user
            User user = getLoggedInUserDetails(principal);

            // Update address in DB
            PaymentCard payment = userService.updatePaymentCard(user.getId(), cardId, paymentCard);

            return ResponseEntity.ok(payment); // return updated address as JSON
        }
        catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/paymentCard/{id}/default")
    @ResponseBody
    public ResponseEntity<String> makeDefaultCard(@PathVariable("id") Long cardId, Principal principal) {
        User user = getLoggedInUserDetails(principal);
        User dbUser = userRepository.findById(user.getId()).orElse(user);

        userService.makeDefaultCard(user.getId(), cardId);
        return ResponseEntity.ok("Default payment card updated successfully!");
    }


    @DeleteMapping("/paymentCard/{id}")
    @ResponseBody
    public ResponseEntity<String> deletePaymentCard(@PathVariable Long id, Principal principal){
        User user = getLoggedInUserDetails(principal);

        PaymentCard paymentCard = userService.getCardById(id);
        if (paymentCard == null || paymentCard.getUser() == null || !Objects.equals(paymentCard.getUser().getId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized action!");
        }

        userService.deleteCard(id);
        return ResponseEntity.ok("Payment Card removed successfully!");
    }


    @PostMapping("/delete-account")
    @ResponseBody
    public Map<String, String> deleteAccount(@RequestBody Map<String, String> data, Principal principal, HttpSession session){
        Map<String, String> response = new HashMap<>();
        try {
            String emailInput = data.get("email");
            User user = getLoggedInUserDetails(principal);

            if(!user.getEmail().equals(emailInput)){
                response.put("status", "error");
                response.put("message", "Email does not match!");
                return response;
            }

            userService.deleteUser(user.getId());
            session.invalidate(); // log out the user

            response.put("status", "success");
            response.put("message", "Account deleted successfully!");
        }
        catch(Exception e){
            response.put("status", "error");
            response.put("message", "Something went wrong!");
        }
        return response;
    }



    @GetMapping("/cart")
    public String cart(Model model, Principal principal){

        model.addAttribute("title", "Cart - Ecommerce Shopping Cart");

        User user = getLoggedInUserDetails(principal);
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        model.addAttribute("carts", carts);

        if (carts.isEmpty()) {
            model.addAttribute("subtotal", 0.0);
            model.addAttribute("shipping", 0.0);
            model.addAttribute("tax", 0.0);
            model.addAttribute("totalOrderPrice", 0.0);
        }
        else {
            double subtotal=cartService.getCartTotalByUser(user.getId());

            String shippingType=cartService.getUserShippingType(user.getId());
            double shippingCost=cartService.getUserShippingCost(user.getId());

            double tax=27.0; // static or dynamic calculation
            double total=subtotal+shippingCost+tax;

            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shippingType", shippingType);
            model.addAttribute("shippingCost", shippingCost);
            model.addAttribute("tax", tax);
            model.addAttribute("totalOrderPrice", total);
        }

        return "user/cart";
    }

    public User getLoggedInUserDetails(Principal principal){
        String email=principal.getName();
        User user=userService.getUserByEmail(email);

        return user;
    }


    @GetMapping("/removeCart")
    @ResponseBody
    public Map<String, Object> removeCart(@RequestParam Long pid, @RequestParam Long uid){
        boolean removed = cartService.removeFromCart(pid, uid);

        Map<String, Object> response = new HashMap<>();
        response.put("success", removed);
        response.put("totalOrderPrice", cartService.getCartTotalByUser(uid)); // update total

        return response;
    }


    @GetMapping("/cartSummaryAjax")
    @ResponseBody
    public Map<String, Object> cartSummaryAjax(@RequestParam Long uid,
                                               @RequestParam(required = false) Long pid,
                                               @RequestParam(required = false) String action,
                                               @RequestParam(required = false) String shipping,
                                               @RequestParam(value = "color", required = false) String color,
                                               @RequestParam(value = "size", required = false) String size) {

        Map<String, Object> response = new HashMap<>();

        // --- Optional cart action ---
        if(pid != null && action != null){
            if("add".equals(action)){
                cartService.saveCart(pid, uid, color, size);
            }
            else if("remove".equals(action)){
                cartService.removeFromCart(pid, uid);
            }
            else if("increase".equals(action)){
                cartService.changeQuantity(pid, uid, 1);
            }
            else if("decrease".equals(action)){
                cartService.changeQuantity(pid, uid, -1);
            }
        }

        // --- ✅ If user selected a new shipping option, update DB ---
        if (shipping != null && !shipping.isEmpty()) {
            cartService.updateShipping(uid, shipping);
        }

        // --- Always recalc summary ---
        double subtotal = cartService.getCartTotalByUser(uid);

        // ✅ Get shipping price directly from DB (from any row, since it's same for user)
        double shippingCost = cartService.getUserShippingCost(uid);

        double tax = 27.0; // Or calculate dynamically
        double total = subtotal+tax+shippingCost;

        int cartCount = cartService.getCountCart(uid);

        // ✅ Always return 2 decimal places
        response.put("success", true);
        response.put("subtotal", String.format("%.2f", subtotal));
        response.put("shipping", String.format("%.2f", shippingCost));
        response.put("tax", String.format("%.2f", tax));
        response.put("totalOrderPrice", String.format("%.2f", total));
        response.put("cartCount", cartCount);

        // ✅ NEW: empty cart flag
        response.put("emptyCart", cartCount == 0);

        // --- Item-level info (if pid was passed & item still exists) ---
        if(pid != null){
            int newQuantity = cartService.getQuantityByProduct(pid, uid);
            if(newQuantity <= 0) {
                response.put("newQuantity", 0);      // ✅ Always return 0 if deleted
                response.put("itemSubtotal", 0.0);
            }
            else{
                response.put("newQuantity", newQuantity);
                response.put("itemSubtotal", cartService.getItemSubtotal(pid, uid));
            }
        }

        return response;
    }


    @PostMapping("/cart/clear")
    @ResponseBody
    public Map<String, Object> clearCart(@RequestParam Long uid){
        Map<String, Object> response = new HashMap<>();
        try{
            cartService.clearCartByUser(uid);
            response.put("success", true);
        } 
        catch(Exception e){
            response.put("success", false);
        }
        return response;
    }

    @GetMapping("/checkout")
    public String checkout(Model model, Principal principal){

        model.addAttribute("title", "Checkout - Ecommerce Shpoping Cart");

        User user = getLoggedInUserDetails(principal);
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        int cartCount = cartService.getCountCart(user.getId());
        double subtotal=cartService.getCartTotalByUser(user.getId());
        double shippingCost = cartService.getUserShippingCost(user.getId());

        double tax = 27.0; // Or calculate dynamically
        double total = subtotal+tax+shippingCost;

        Address defaultAddress = user.getAddresses()
                .stream()
                .filter(Address::getIsDefault)
                .findFirst()
                .orElse(null);

        model.addAttribute("carts", carts);
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingCost", shippingCost);
        model.addAttribute("totalOrderPrice", total);
        model.addAttribute("defaultAddress", defaultAddress);

        return "/user/checkout";
    }

    // creating order for payment
    @PostMapping("/create_order")
    @ResponseBody
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request, Principal principal) throws Exception {
        System.out.println(request);
        String email = principal.getName();
        User user = userService.getUserByEmail(email);

        double amountInRupees = request.getAmount();
        int amountInPaise = (int) Math.round(amountInRupees * 100);

        RazorpayClient client = new RazorpayClient("rzp_test_YfFqvXPL8JPBdJ", "BvhNZhnzP15Umh3AfSTDp3bm");
        JSONObject ob= new JSONObject();
        ob.put("amount", amountInPaise); // Razorpay wants paise
        ob.put("currency", "INR");
        ob.put("receipt", "txn_" + UUID.randomUUID());

        // ✅ Create order in Razorpay
        com.razorpay.Order razorpayOrder = client.orders.create(ob);
        System.out.println("Razorpay Order: " + razorpayOrder);

        double totalAmount = amountInPaise / 100.0;
        // ✅ Save order in DB (ONE order + MANY items + ONE address)
        orderService.saveOrder(user.getId(), request, razorpayOrder, totalAmount);
        // ✅ Return Razorpay order response to frontend
        return ResponseEntity.ok(razorpayOrder.toJson().toMap());
    }

    @PostMapping("/update_order")
    public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data) {

        String orderId = data.get("order_id").toString();

        // Fetch single order (now unique per Razorpay orderId)
        com.ecommerce.model.Orders.Order order = this.orderRepository.findByOrderId(orderId);
        if (order == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Order not found"));
        }

        // Update payment info
        order.setPaymentId(data.get("payment_id").toString());
        order.setPaymentStatus(data.get("status").toString());

        if (data.containsKey("payment_type")) {
            order.setPaymentType(data.get("payment_type").toString());
        }

        this.orderRepository.save(order);

        // ✅ Send confirmation mail only if payment was successful
        if ("Paid".equalsIgnoreCase(order.getPaymentStatus())) {
            emailUtil.sendMailForOrder(order, "Order Confirmation");
        }

        // ✅ Build OrderItemResponse list
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        (long) i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getPrice()
                ))
                .toList();

        // ✅ Build OrderResponse
        OrderResponse orderResponse = new OrderResponse(
                order.getOrderId(),
                order.getItems().stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum(),
                order.getOrderStatus(),
                order.getPaymentStatus(),
                order.getPaymentType(),
                order.getPaymentId(),
                order.getOrderDate(),
                order.getOrderAddress(),
                itemResponses
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("orderId", order.getOrderId());
        response.put("order", orderResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/success")
    public String OrderConfirmation(@RequestParam("orderId") String orderId, Model model){

        com.ecommerce.model.Orders.Order order=orderService.findByOrderId(orderId);
        model.addAttribute("order", order);

        List<OrderStatus> steps;
        int currentStepIndex = 0;

        if ("CANCELLED".equalsIgnoreCase(order.getOrderStatus())){
            steps=List.of(OrderStatus.CANCELLED);
            currentStepIndex = 0;
        }
        else if("SUCCESS".equalsIgnoreCase(order.getOrderStatus())){
            steps=List.of(OrderStatus.SUCCESS);
            currentStepIndex = 0;
        }
        else{

            steps = List.of(
                    OrderStatus.IN_PROGRESS,
                    OrderStatus.RECEIVED,
                    OrderStatus.PACKED,
                    OrderStatus.OUT_FOR_DELIVERY,
                    OrderStatus.DELIVERED
            );

            // find the index of current order status
            for (int i = 0; i < steps.size(); i++) {
                if (steps.get(i).getName().equalsIgnoreCase(order.getOrderStatus())) {
                    currentStepIndex = i;
                    break;
                }
            }
        }

        model.addAttribute("steps", steps);
        model.addAttribute("currentStepIndex", currentStepIndex);

        return "/user/paymentsuccess";
    }


}
