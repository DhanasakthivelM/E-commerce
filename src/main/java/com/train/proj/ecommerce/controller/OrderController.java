package com.train.proj.ecommerce.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.train.proj.ecommerce.entity.Cart;
import com.train.proj.ecommerce.entity.Order;
import com.train.proj.ecommerce.entity.Payment;
import com.train.proj.ecommerce.entity.Product;
import com.train.proj.ecommerce.entity.User;
import com.train.proj.ecommerce.repository.CartRepository;
import com.train.proj.ecommerce.repository.OrderRepository;
import com.train.proj.ecommerce.repository.PaymentRepository;
import com.train.proj.ecommerce.repository.ProductRepository;
import com.train.proj.ecommerce.repository.UserRepository;

@Controller
@RequestMapping("/customer")
public class OrderController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderController(OrderRepository orderRepository, PaymentRepository paymentRepository, 
                          CartRepository cartRepository, UserRepository userRepository,
                          ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @PostMapping("/place-order")
    public String placeOrder(@RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String address,
                           @RequestParam String city,
                           @RequestParam String state,
                           @RequestParam String pinCode,
                           @RequestParam String phone,
                           @RequestParam String paymentMethod,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/login";
        }
        
        List<Cart> cartItems = cartRepository.findByUserIdWithProductAndUser(user.getUserId());
        
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Your cart is empty");
            return "redirect:/customer/viewcart";
        }
        
        // Calculate totals
        double subtotal = cartItems.stream()
            .mapToDouble(cart -> cart.getProduct().getPrice() * cart.getQuantity())
            .sum();
        double tax = subtotal * 0.18; // 18% GST
        double total = subtotal + tax;
        
        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount((float) total);
        order.setOrderDate(LocalDate.now());
        order.setStatus(Order.Status.PENDING);
        
        Order savedOrder = orderRepository.save(order); 
        
        // Reduce stock quantities for all cart items
        for (Cart cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int newStock = product.getStockQuantity() - cartItem.getQuantity();
            product.setStockQuantity(Math.max(0, newStock)); // Ensure stock doesn't go negative
            productRepository.save(product); // Save updated stock
        }
        
        // Create payment record
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setAmount((float) total);
        payment.setPaymentDate(LocalDate.now());
        
        // Process payment based on method
        if ("cod".equals(paymentMethod)) {
            payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
            paymentRepository.save(payment);
            
            // Clear cart after successful order
            for (Cart cartItem : cartItems) {
                cartRepository.delete(cartItem);
            }
            
            redirectAttributes.addFlashAttribute("message", "Order placed successfully! You will pay on delivery.");
            return "redirect:/customer/order-confirmation/" + savedOrder.getOrderId();
            
        } else {
            // For future payment methods (card, UPI)
            payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
            paymentRepository.save(payment);
            
            // Redirect to payment processing
            return "redirect:/customer/process-payment/" + savedOrder.getOrderId();
        }
        
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while placing your order. Please try again.");
            return "redirect:/customer/viewcart";
        }
    }

    @GetMapping("/process-payment/{orderId}")
    public String processPayment(@PathVariable Integer orderId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Order order = orderRepository.findByIdWithUserAndPayments(orderId);
        
        if (order == null) {
            return "redirect:/customer/viewcart";
        }
        
        model.addAttribute("order", order);
        return "payment-processing";
    }

    @PostMapping("/confirm-payment/{orderId}")
    public String confirmPayment(@PathVariable Integer orderId,
                                @RequestParam String paymentMethod,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Order order = orderRepository.findById(orderId).orElse(null);
        
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Order not found");
            return "redirect:/customer/viewcart";
        }
        
        // Simulate payment processing
        List<Payment> payments = paymentRepository.findByOrderIdWithOrder(orderId);
        
        if (!payments.isEmpty()) {
            Payment payment = payments.get(0);
            // Simulate successful payment (in real app, integrate with payment gateway)
            payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
            paymentRepository.save(payment);
            
            // Update order status
            order.setStatus(Order.Status.PENDING);
            orderRepository.save(order);
            
            // Clear cart
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            List<Cart> cartItems = cartRepository.findByUserIdWithProductAndUser(user.getUserId());
            for (Cart cartItem : cartItems) {
                cartRepository.delete(cartItem);
            }
        }
        
        redirectAttributes.addFlashAttribute("message", "Payment successful! Your order has been confirmed.");
        return "redirect:/customer/order-confirmation/" + orderId;
    }

    @GetMapping("/order-confirmation/{orderId}")
    public String orderConfirmation(@PathVariable Integer orderId, Model model, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }
            
            // Try to get order with fallback
            Order order = null;
            try {
                order = orderRepository.findByIdWithUserAndPayments(orderId);
            } catch (Exception e) {
                order = orderRepository.findById(orderId).orElse(null);
            }
            
            if (order == null) {
                return "redirect:/customer/my-orders";
            }
            
            // Try to get payments with fallback
            List<Payment> payments = null;
            try {
                payments = paymentRepository.findByOrderIdWithOrder(orderId);
            } catch (Exception e) {
                payments = new java.util.ArrayList<>();
            }
            
            model.addAttribute("order", order);
            model.addAttribute("payments", payments);
            
            return "order-confirmation";
            
        } catch (Exception e) {
            return "redirect:/customer/my-orders";
        }
    }

    @GetMapping("/my-orders")
    public String myOrders(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Order> orders = orderRepository.findByUserIdWithUser(user.getUserId());
        
        model.addAttribute("orders", orders);
        model.addAttribute("user", user);
        
        return "my-orders";
    }

    @PostMapping("/cancel-order/{orderId}")
    public String cancelOrder(@PathVariable Integer orderId, 
                             Authentication authentication, 
                             RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/login";
            }
            
            Order order = orderRepository.findById(orderId).orElse(null);
            
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Order not found");
                return "redirect:/customer/my-orders";
            }
            
            // Check if user owns this order
            if (order.getUser().getUserId() != user.getUserId()) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to cancel this order");
                return "redirect:/customer/my-orders";
            }
            
            // Check if order can be cancelled (only PENDING or CONFIRMED orders)
            if (order.getStatus() == Order.Status.SHIPPED || 
            	    order.getStatus() == Order.Status.DELIVERED || 
            	    order.getStatus() == Order.Status.CANCELLED) {
            	    redirectAttributes.addFlashAttribute("error", "This order cannot be cancelled");
            	    return "redirect:/customer/my-orders";
            	}
            
            // Update order status to CANCELLED
            order.setStatus(Order.Status.CANCELLED);
            orderRepository.save(order);
            
            // Update payment status to FAILED if exists
            List<Payment> payments = paymentRepository.findByOrderIdWithOrder(orderId);
            for (Payment payment : payments) {
                if (payment.getPaymentStatus() == Payment.PaymentStatus.PENDING) {
                    payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                }
            }
            
            redirectAttributes.addFlashAttribute("message", "Order cancelled successfully");
            return "redirect:/customer/my-orders";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while cancelling the order");
            return "redirect:/customer/my-orders";
        }
    }

} 