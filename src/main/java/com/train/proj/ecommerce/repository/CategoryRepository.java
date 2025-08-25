package com.train.proj.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.train.proj.ecommerce.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

}
