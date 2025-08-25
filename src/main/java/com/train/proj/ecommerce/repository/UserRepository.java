package com.train.proj.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.train.proj.ecommerce.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	 User findByUsername(String username);
	 User findByEmail(String email);
}
