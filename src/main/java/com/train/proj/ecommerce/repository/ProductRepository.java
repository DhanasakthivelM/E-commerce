package com.train.proj.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.train.proj.ecommerce.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
	List<Product> findByNameContaining(String name);
	
	@Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.productId = :id")
	Optional<Product> findByIdWithCategory(@Param("id") Integer id);
}

