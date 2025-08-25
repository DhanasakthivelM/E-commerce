package com.train.proj.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.train.proj.ecommerce.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    @Query("SELECT p FROM Payment p JOIN FETCH p.order WHERE p.order.orderId = :orderId")
    List<Payment> findByOrderIdWithOrder(@Param("orderId") Integer orderId);
} 