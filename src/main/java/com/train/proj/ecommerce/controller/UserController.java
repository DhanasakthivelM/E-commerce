package com.train.proj.ecommerce.controller;

import com.train.proj.ecommerce.entity.User;
import com.train.proj.ecommerce.entity.User.Role;
import com.train.proj.ecommerce.service.UserServiceInterface;
import com.train.proj.ecommerce.exception.UserNotFoundException;
import com.train.proj.ecommerce.exception.InvalidPasswordException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private UserServiceInterface userService;

    // Main home page (both public and authenticated users)
    @GetMapping("/")
    public String homePage(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal, 
                          @RequestParam(value = "products", required = false) String products,
                          @RequestParam(value = "cart", required = false) String cart,
                          @RequestParam(value = "orders", required = false) String orders,
                          Model model) {
        if (principal != null) {
            // User is authenticated, add user data
            User user = userService.getUserByUsername(principal.getUsername());
            model.addAttribute("user", user);
        }
        
        // Handle page messages
        if (products != null) {
            model.addAttribute("message", "Products page is coming soon! Currently under development.");
        } else if (cart != null) {
            model.addAttribute("message", "Cart functionality is coming soon! Currently under development.");
        } else if (orders != null) {
            model.addAttribute("message", "Order history is coming soon! Currently under development.");
        }
        
        return "home";
    }

    // Redirect /home to root
    @GetMapping("/home")
    public String redirectToHome() {
        return "redirect:/";
    }

    // Registration form
    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Handle registration
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        try {
            user.setRole(Role.CUSTOMER); // default role
            userService.registerUser(user);
            model.addAttribute("message", "Registration successful!");
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("user", user);
            return "register";
        }
    }

    // Login form
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "registered", required = false) String registered,
                               @RequestParam(value = "logout", required = false) String logout,
                               @RequestParam(value = "error", required = false) String error,
                               Model model) {
        if (registered != null) {
            model.addAttribute("message", "Registration successful! Please login.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        return "login";
    }



    // View profile — only if logged in
    @GetMapping("/user/{userId}")
    public String getUserProfile(@PathVariable int userId,
                                 @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                                 Model model) {
        User loggedInUser = userService.getUserByUsername(principal.getUsername());
        if (loggedInUser == null || (loggedInUser.getUserId() != userId && !loggedInUser.getRole().name().equals("ADMIN"))) {
            return "redirect:/";
        }
        try {
            User user = userService.getUserProfile(userId);
            model.addAttribute("user", user);
            return "profile";
        } catch (UserNotFoundException e) {
            return "redirect:/";
        }
    }



    // Edit profile form — only if logged in
    @GetMapping("/user/{userId}/edit")
    public String showEditProfileForm(@PathVariable int userId,
                                      @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                                      Model model) {
        User loggedInUser = userService.getUserByUsername(principal.getUsername());
        if (loggedInUser == null || (loggedInUser.getUserId() != userId && !loggedInUser.getRole().name().equals("ADMIN"))) {
            return "redirect:/";
        }
        try {
            User user = userService.getUserProfile(userId);
            model.addAttribute("user", user);
            return "edit_profile";
        } catch (UserNotFoundException e) {
            return "redirect:/";
        }
    }

    // Submit profile update — only if logged in
    @PostMapping("/user/{userId}")
    public String updateUserProfile(@PathVariable int userId,
                                    @ModelAttribute User updatedUser,
                                    @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                                    RedirectAttributes redirectAttributes) {
        User loggedInUser = userService.getUserByUsername(principal.getUsername());
        if (loggedInUser == null || (loggedInUser.getUserId() != userId && !loggedInUser.getRole().name().equals("ADMIN"))) {
            return "redirect:/";
        }
        try {
            userService.updateUserProfile(userId, updatedUser);
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
            return "redirect:/";
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
            return "redirect:/";
        }
    }

    // Change password form
    @GetMapping("/user/{userId}/change-password")
    public String showChangePasswordForm(@PathVariable int userId,
                                        @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                                        Model model) {
        User loggedInUser = userService.getUserByUsername(principal.getUsername());
        if (loggedInUser == null || loggedInUser.getUserId() != userId) {
            return "redirect:/";
        }
        model.addAttribute("userId", userId);
        return "change_password";
    }

    // Handle password change
    @PostMapping("/user/{userId}/change-password")
    public String changePassword(@PathVariable int userId,
                                @RequestParam("currentPassword") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                                RedirectAttributes redirectAttributes) {
        User loggedInUser = userService.getUserByUsername(principal.getUsername());
        if (loggedInUser == null || loggedInUser.getUserId() != userId) {
            return "redirect:/";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters long.");
            return "redirect:/";
        }

        try {
            userService.updatePassword(userId, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("message", "Password changed successfully!");
            return "redirect:/";
        } catch (InvalidPasswordException e) {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect.");
            return "redirect:/";
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to change password: " + e.getMessage());
            return "redirect:/";
        }
    }
}
