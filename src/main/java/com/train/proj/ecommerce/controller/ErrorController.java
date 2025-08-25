package com.train.proj.ecommerce.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorController {

    /**
     * Access Denied page
     * Shows when user tries to access a protected resource without proper authorization
     */
    @GetMapping("/access-denied")
    public String accessDenied(HttpServletRequest request, Model model) {
        // Get current authentication info for debugging
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the access denied attempt
        System.out.println("=== ACCESS DENIED ===");
        System.out.println("Requested URL: " + request.getRequestURL());
        System.out.println("User: " + (auth != null ? auth.getName() : "Anonymous"));
        System.out.println("Authorities: " + (auth != null ? auth.getAuthorities() : "None"));
        System.out.println("====================");
        
        // Add user info to model for display
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            model.addAttribute("username", auth.getName());
            model.addAttribute("userRole", auth.getAuthorities().toString());
            model.addAttribute("isLoggedIn", true);
        } else {
            model.addAttribute("isLoggedIn", false);
        }
        
        return "access-denied";
    }

    /**
     * General error page
     * Can be used for other error scenarios
     */
    @GetMapping("/error")
    public String error(HttpServletRequest request, Model model) {
        System.out.println("=== GENERAL ERROR ===");
        System.out.println("Requested URL: " + request.getRequestURL());
        System.out.println("====================");
        
        return "access-denied"; // For now, redirect to access denied page
    }
} 