package com.train.proj.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.train.proj.ecommerce.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.user.userId = :userId ORDER BY o.orderDate DESC")
    List<Order> findByUserIdWithUser(@Param("userId") Integer userId);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user ORDER BY o.orderDate DESC")
    List<Order> findAllWithUser();
    
    @Query("SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.payments WHERE o.orderId = :orderId")
    Order findByIdWithUserAndPayments(@Param("orderId") Integer orderId);
} 