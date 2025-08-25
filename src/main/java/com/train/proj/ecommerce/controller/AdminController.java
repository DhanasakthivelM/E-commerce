package com.train.proj.ecommerce.controller;


import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.train.proj.ecommerce.entity.Product;
import com.train.proj.ecommerce.entity.Category;
import com.train.proj.ecommerce.entity.Order;
import com.train.proj.ecommerce.entity.Payment;
import com.train.proj.ecommerce.entity.User;
import com.train.proj.ecommerce.entity.User.Role;
import com.train.proj.ecommerce.exception.UserNotFoundException;
import com.train.proj.ecommerce.repository.ProductRepository;
import com.train.proj.ecommerce.repository.CategoryRepository;
import com.train.proj.ecommerce.repository.OrderRepository;
import com.train.proj.ecommerce.repository.PaymentRepository;
import com.train.proj.ecommerce.service.UserServiceInterface;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserServiceInterface userService;

    // Admin login page (Spring Security will handle actual authentication)
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;


    public AdminController(ProductRepository productRepository, CategoryRepository categoryRepository, OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }
    
    @GetMapping("/productadd-form")
    public String showAddForm(Model model,@RequestParam(value = "success", required = false) String success) {
    	Product product=new Product();
    	model.addAttribute("product",product);
    	model.addAttribute("ans", success != null && success.equals("true"));
    	// Add categories to the model for dropdown
    	model.addAttribute("categories", categoryRepository.findAll());
    	return "productaddform";
    }
    @PostMapping("/productadd-form")
    public String addProduct(
            @ModelAttribute("product") Product product,
            Model model,
            @RequestParam("imageFile") MultipartFile imageFile
    ) throws IOException {
    	 Product product1 = new Product();
         product1.setName(product.getName());
         product1.setPrice(product.getPrice());
         
         // Properly load and set the category
         Optional<Category> categoryOptional = categoryRepository.findById(product.getCategoryId());
         if (categoryOptional.isPresent()) {
             product1.setCategory(categoryOptional.get());
         }
         
         product1.setDescription(product.getDescription());
         product1.setImageData(imageFile.getBytes());
         product1.setStockQuantity(product.getStockQuantity());

        productRepository.save(product1);
        return "redirect:/admin/productadd-form?success=true";
    }


    @GetMapping("/productupdate-form/{id}")
    public String showUpdateForm(Model model,
                                 @RequestParam(value = "success", required = false) String success,
                                 @PathVariable("id") int id) {
        Optional<Product> productOptional = productRepository.findByIdWithCategory(id);
        if (productOptional.isEmpty()) {
            return "update-failure";
        }
        Product product = productOptional.get();
        model.addAttribute("product", product);
        model.addAttribute("ans", "true".equals(success));
        // Add categories to the model for dropdown
        model.addAttribute("categories", categoryRepository.findAll());
        return "productupdateform";
    }
    

    
    @PostMapping("/productupdate-form")
    public String updateProduct(
            @ModelAttribute("product") Product product,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model) throws IOException {

        Optional<Product> optionalProduct = productRepository.findByIdWithCategory(product.getProductId());
        if (optionalProduct.isEmpty()) {
            model.addAttribute("error", "Product not found");
            return "update-failure";  
        }

        Product existingProduct = optionalProduct.get();

        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        
        // Properly load and set the category instead of using setCategoryId
        Optional<Category> categoryOptional = categoryRepository.findById(product.getCategoryId());
        if (categoryOptional.isPresent()) {
            existingProduct.setCategory(categoryOptional.get());
        }
        
        existingProduct.setDescription(product.getDescription());
        existingProduct.setStockQuantity(product.getStockQuantity());
        if (imageFile != null && !imageFile.isEmpty()) {
            existingProduct.setImageData(imageFile.getBytes());
        }
        productRepository.save(existingProduct);
        return "redirect:/admin/allproducts"; 
    }

    
    @GetMapping("/productdelete-form/{id}")
    public String deleteProductitem(@PathVariable("id") int id) {
        productRepository.deleteById(id);  
        return "redirect:/admin/allproducts";  
    }

  
    @GetMapping("/allproducts")
    public String getAllProducts(Model model) {
        List<Product> allproducts=productRepository.findAll();
        model.addAttribute("allproducts",allproducts);
        return "allproducts-form";
    }

    // Category Management Methods
    @GetMapping("/categories/add")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "categoryaddform";
    }

    @PostMapping("/categories/add")
    public String addCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Category name is required!");
                return "redirect:/admin/categories/add";
            }
            
            // Check if category already exists
            List<Category> existingCategories = categoryRepository.findAll();
            for (Category existingCategory : existingCategories) {
                if (existingCategory.getName().equalsIgnoreCase(category.getName().trim())) {
                    redirectAttributes.addFlashAttribute("error", "Category '" + category.getName() + "' already exists!");
                    return "redirect:/admin/categories/add";
                }
            }
            
            category.setName(category.getName().trim());
            categoryRepository.save(category);
            redirectAttributes.addFlashAttribute("message", "Category '" + category.getName() + "' added successfully!");
            return "redirect:/admin/productadd-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add category. Please try again.");
            return "redirect:/admin/categories/add";
        }
    }

  
    
    @GetMapping("/admin/login")
    @PreAuthorize("permitAll()")
    public String showAdminLoginPage(@RequestParam(value = "logout", required = false) String logout,
                                    @RequestParam(value = "error", required = false) String error,
                                    Model model) {
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        return "admin_login";
    }

    // Admin dashboard — accessible only to authenticated users with ADMIN role
    @GetMapping("/dashboard")
    public String adminDashboard(Principal principal,
                                 Model model) {
        User admin = userService.getUserByUsername(principal.getName());
        List<User> allUsers = userService.getAllUsers();
        
        // Get only first 5 users for dashboard preview
        List<User> recentUsers = allUsers.stream()
                .limit(5)
                .toList();
        
        // Get order statistics
        List<Order> allOrders = orderRepository.findAll();
        
        model.addAttribute("admin", admin);
        model.addAttribute("users", recentUsers);  // Only 5 users for dashboard
        model.addAttribute("totalUsers", allUsers.size());  // But show total count in stats
        model.addAttribute("customerCount", allUsers.stream().mapToInt(u -> u.getRole() == Role.CUSTOMER ? 1 : 0).sum());
        model.addAttribute("adminCount", allUsers.stream().mapToInt(u -> u.getRole() == Role.ADMIN ? 1 : 0).sum());
        model.addAttribute("totalOrders", allOrders.size());
        
        return "admin_dashboard";
    }

    // Admin view all users
    @GetMapping("/users")
    public String viewAllUsers(@RequestParam(value = "search", required = false) String search,
                              Model model) {
        List<User> users;
        if (search != null && !search.trim().isEmpty()) {
            // Simple search by username or email
            users = userService.getAllUsers().stream()
                    .filter(user -> user.getUsername().toLowerCase().contains(search.toLowerCase()) ||
                                  user.getEmail().toLowerCase().contains(search.toLowerCase()))
                    .toList();
            model.addAttribute("searchTerm", search);
        } else {
            users = userService.getAllUsers();
        }
        
        model.addAttribute("users", users);
        return "admin_users";
    }

    // Admin view specific user
    @GetMapping("/users/{userId}")
    public String viewUser(@PathVariable int userId, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserProfile(userId);
            model.addAttribute("user", user);
            return "admin_user_detail";
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }
    }

//    // Admin create user form
//    @GetMapping("/users/new")
//    public String showCreateUserForm(Model model) {
//        model.addAttribute("user", new User());
//        model.addAttribute("roles", Role.values());
//        return "admin_create_user";
//    }

//    // Admin create user
//    @PostMapping("/users")
//    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
//        try {
//            userService.registerUser(user);
//            redirectAttributes.addFlashAttribute("message", "User created successfully!");
//            return "redirect:/admin/users";
//        } catch (UserAlreadyExistsException e) {
//            redirectAttributes.addFlashAttribute("error", "User already exists: " + e.getMessage());
//            return "redirect:/admin/users/new";
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Failed to create user: " + e.getMessage());
//            return "redirect:/admin/users/new";
//        }
//    }

    // Admin edit user form
    @GetMapping("/users/{userId}/edit")
    public String showEditUserForm(@PathVariable int userId, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserProfile(userId);
            model.addAttribute("user", user);
            model.addAttribute("roles", Role.values());
            return "admin_edit_user";
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }
    }

    // Admin update user
    @PostMapping("/users/{userId}/update")
    public String updateUser(@PathVariable int userId, @ModelAttribute User updatedUser, 
                            RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserProfile(userId, updatedUser);
            redirectAttributes.addFlashAttribute("message", "User updated successfully!");
            return "redirect:/admin/users/" + userId;
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update user: " + e.getMessage());
            return "redirect:/admin/users/" + userId + "/edit";
        }
    }

    // Admin delete user
    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable int userId, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("message", "User deleted successfully!");
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // Admin logout — handled by Spring Security
    @GetMapping("/logout-success")
    @PreAuthorize("permitAll()")
    public String logoutSuccess() {
        return "redirect:/admin/login?logout";
    }

    // Admin Orders Management
    @GetMapping("/orders")
    @Transactional(readOnly = true)
    public String viewAllOrders(Model model) {
        try {
            // Use findAll() as fallback if findAllWithUser() causes issues
            List<Order> orders;
            try {
                orders = orderRepository.findAllWithUser();
            } catch (Exception e) {
                // Fallback to basic findAll
                orders = orderRepository.findAll();
            }
            
            model.addAttribute("orders", orders);
            model.addAttribute("message", "Orders loaded successfully");
            return "admin-orders";
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            model.addAttribute("error", "Unable to load orders: " + e.getMessage());
            model.addAttribute("orders", new java.util.ArrayList<>());
            return "admin-orders";
        }
    }

    @PostMapping("/orders/{orderId}/update-status")
    @Transactional
    public String updateOrderStatus(@PathVariable Integer orderId, 
                                  @RequestParam String status,
                                  RedirectAttributes redirectAttributes) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            try {
                Order.Status newStatus = Order.Status.valueOf(status.toUpperCase());
                order.setStatus(newStatus);
                orderRepository.save(order);
                redirectAttributes.addFlashAttribute("message", "Order status updated to " + newStatus + " successfully!");
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", "Invalid status value!");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Order not found!");
        }
        
        return "redirect:/admin/orders";
    }
    
    @PostMapping("/orders/{orderId}/update-payment")
    @Transactional
    public String updatePaymentStatus(@PathVariable Integer orderId, 
                                    @RequestParam String paymentStatus,
                                    RedirectAttributes redirectAttributes) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Order not found!");
                return "redirect:/admin/orders";
            }
            
            // Get payments for this order
            List<Payment> payments = paymentRepository.findByOrderIdWithOrder(orderId);
            
            if (payments.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No payment records found for this order!");
                return "redirect:/admin/orders";
            }
            
            // Update payment status
            Payment.PaymentStatus newStatus = Payment.PaymentStatus.valueOf(paymentStatus.toUpperCase());
            for (Payment payment : payments) {
                payment.setPaymentStatus(newStatus);
                paymentRepository.save(payment);
            }
            
            redirectAttributes.addFlashAttribute("message", "Payment status updated to " + newStatus + " successfully!");
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid payment status value!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating payment status: " + e.getMessage());
        }
        
        return "redirect:/admin/orders";
    }
    
    @PostMapping("/orders/{orderId}/cancel")
    @Transactional
    public String cancelOrder(@PathVariable Integer orderId, 
                            RedirectAttributes redirectAttributes) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Order not found!");
                return "redirect:/admin/orders";
            }
            
            // Check if order can be cancelled
            if (order.getStatus() == Order.Status.DELIVERED) {
                redirectAttributes.addFlashAttribute("error", "Cannot cancel a delivered order!");
                return "redirect:/admin/orders";
            }
            
            if (order.getStatus() == Order.Status.CANCELLED) {
                redirectAttributes.addFlashAttribute("error", "Order is already cancelled!");
                return "redirect:/admin/orders";
            }
            
            // Cancel the order
            order.setStatus(Order.Status.CANCELLED);
            orderRepository.save(order);
            
            // Update payment status to failed if exists
            List<Payment> payments = paymentRepository.findByOrderIdWithOrder(orderId);
            for (Payment payment : payments) {
                if (payment.getPaymentStatus() != Payment.PaymentStatus.COMPLETED) {
                    payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                }
            }
            
            redirectAttributes.addFlashAttribute("message", "Order cancelled successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling order: " + e.getMessage());
        }
        
        return "redirect:/admin/orders";
    }
    
    // Simple debug endpoint to test admin orders access
    @GetMapping("/orders-debug")
    public String debugOrders(Model model) {
        model.addAttribute("orders", new java.util.ArrayList<>());
        model.addAttribute("message", "Debug: Admin orders page accessible!");
        return "admin-orders";
    }
}
