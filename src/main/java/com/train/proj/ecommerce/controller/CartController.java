package com.train.proj.ecommerce.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.train.proj.ecommerce.entity.Cart;
import com.train.proj.ecommerce.entity.Product;
import com.train.proj.ecommerce.entity.User;
import com.train.proj.ecommerce.repository.CartRepository;
import com.train.proj.ecommerce.repository.ProductRepository;
import com.train.proj.ecommerce.repository.UserRepository;

@Controller
@RequestMapping("/customer")
public class CartController {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartController(CartRepository cartRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/viewcart")
    @Transactional(readOnly = true)
    public String viewCart(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Cart> cartItems = cartRepository.findByUserIdWithProductAndUser(user.getUserId());
        
        // Calculate total
        double totalPrice = cartItems.stream()
            .mapToDouble(cart -> cart.getProduct().getPrice() * cart.getQuantity())
            .sum();
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("user", user);
        
        return "viewcart";
    }

    @PostMapping("/addtocart/{productId}")
    @Transactional
    public String addToCart(@PathVariable Integer productId, 
                          @RequestParam(defaultValue = "1") Integer quantity,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Please login to add items to cart");
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        Product product = productRepository.findById(productId).orElse(null);
        
        if (user == null || product == null) {
            redirectAttributes.addFlashAttribute("error", "Product or user not found");
            return "redirect:/products/allproducts";
        }
        
        // Check stock availability
        if (product.getStockQuantity() <= 0) {
            redirectAttributes.addFlashAttribute("error", "Product is out of stock!");
            return "redirect:/products/viewproduct/" + productId;
        }
        
        // Check if product already exists in cart
        try {
            Cart existingCart = cartRepository.findByUserIdAndProductId(user.getUserId(), productId);
            
            if (existingCart != null) {
                // Check if adding more exceeds stock
                int newQuantity = existingCart.getQuantity() + quantity;
                if (newQuantity > product.getStockQuantity()) {
                    redirectAttributes.addFlashAttribute("error", "Cannot add " + quantity + " more items. Only " + 
                        (product.getStockQuantity() - existingCart.getQuantity()) + " items available!");
                    return "redirect:/products/viewproduct/" + productId;
                }
                // Update quantity
                existingCart.setQuantity(newQuantity);
                cartRepository.save(existingCart);
            } else {
                // Check if requested quantity exceeds stock
                if (quantity > product.getStockQuantity()) {
                    redirectAttributes.addFlashAttribute("error", "Only " + product.getStockQuantity() + " items available in stock!");
                    return "redirect:/products/viewproduct/" + productId;
                }
                // Create new cart item
                Cart cart = new Cart(user, product, quantity);
                cartRepository.save(cart);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add product to cart: " + e.getMessage());
            return "redirect:/products/viewproduct/" + productId;
        }
        
        redirectAttributes.addFlashAttribute("message", "Product added to cart successfully!");
        return "redirect:/products/viewproduct/" + productId;
    }

    @PostMapping("/updatecart/{cartId}")
    @Transactional
    public String updateCart(@PathVariable Integer cartId, 
                           @RequestParam Integer quantity,
                           RedirectAttributes redirectAttributes) {
        
        Cart cart = cartRepository.findById(cartId).orElse(null);
        
        if (cart == null) {
            redirectAttributes.addFlashAttribute("error", "Cart item not found");
            return "redirect:/customer/viewcart";
        }
        
        if (quantity <= 0) {
            cartRepository.delete(cart);
            redirectAttributes.addFlashAttribute("message", "Item removed from cart");
        } else {
            // Get product separately to avoid lazy loading issues
            Product product = productRepository.findById(cart.getProduct().getProductId()).orElse(null);
            
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Product not found");
                return "redirect:/customer/viewcart";
            }
            
            // Check stock availability
            if (quantity > product.getStockQuantity()) {
                redirectAttributes.addFlashAttribute("error", "Cannot update quantity. Only " + product.getStockQuantity() + " items available in stock!");
                return "redirect:/customer/viewcart";
            }
            
            cart.setQuantity(quantity);
            cartRepository.save(cart);
            redirectAttributes.addFlashAttribute("message", "Cart updated successfully");
        }
        
        return "redirect:/customer/viewcart";
    }

    @PostMapping("/removefromcart/{cartId}")
    @Transactional
    public String removeFromCart(@PathVariable Integer cartId, RedirectAttributes redirectAttributes) {
        
        Cart cart = cartRepository.findById(cartId).orElse(null);
        
        if (cart != null) {
            cartRepository.delete(cart);
            redirectAttributes.addFlashAttribute("message", "Item removed from cart successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Cart item not found");
        }
        
        return "redirect:/customer/viewcart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Cart> cartItems = cartRepository.findByUserIdWithProductAndUser(user.getUserId());
        
        if (cartItems.isEmpty()) {
            return "redirect:/customer/viewcart";
        }
        
        // Calculate totals
        double subtotal = cartItems.stream()
            .mapToDouble(cart -> cart.getProduct().getPrice() * cart.getQuantity())
            .sum();
        double tax = subtotal * 0.18; // 18% GST
        double total = subtotal + tax;
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("tax", tax);
        model.addAttribute("total", total);
        model.addAttribute("user", user);
        
        return "checkout";
    }

    @PostMapping("/buy-now/{productId}")
    @Transactional
    public String buyNow(@PathVariable Integer productId,
                        @RequestParam(defaultValue = "1") Integer quantity,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Please login to buy products");
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        Product product = productRepository.findById(productId).orElse(null);
        
        if (user == null || product == null) {
            redirectAttributes.addFlashAttribute("error", "Product or user not found");
            return "redirect:/products/allproducts";
        }
        
        // Check stock availability
        if (product.getStockQuantity() <= 0) {
            redirectAttributes.addFlashAttribute("error", "Product is out of stock!");
            return "redirect:/products/viewproduct/" + productId;
        }
        
        if (quantity > product.getStockQuantity()) {
            redirectAttributes.addFlashAttribute("error", "Only " + product.getStockQuantity() + " items available in stock!");
            return "redirect:/products/viewproduct/" + productId;
        }
        
        // Clear existing cart (optional - for buy now we want fresh start)
        List<Cart> existingCartItems = cartRepository.findByUserIdWithProductAndUser(user.getUserId());
        for (Cart item : existingCartItems) {
            cartRepository.delete(item);
        }
        
        // Add this product to cart
        try {
            Cart cart = new Cart(user, product, quantity);
            cartRepository.save(cart);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add product to cart: " + e.getMessage());
            return "redirect:/products/viewproduct/" + productId;
        }
        
        redirectAttributes.addFlashAttribute("message", "Product added! Proceed to checkout.");
        return "redirect:/customer/checkout";
    }
} 