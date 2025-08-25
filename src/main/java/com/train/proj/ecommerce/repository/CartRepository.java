package com.train.proj.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.train.proj.ecommerce.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    
    @Query("SELECT c FROM Cart c JOIN FETCH c.product p JOIN FETCH p.category JOIN FETCH c.user WHERE c.user.userId = :userId")
    List<Cart> findByUserIdWithProductAndUser(@Param("userId") Integer userId);
    
    @Query("SELECT c FROM Cart c WHERE c.user.userId = :userId AND c.product.productId = :productId")
    Cart findByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Cart c WHERE c.user.userId = :userId AND c.product.productId = :productId")
    void deleteByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);
} 