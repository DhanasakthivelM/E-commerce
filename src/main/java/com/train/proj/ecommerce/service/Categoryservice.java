package com.train.proj.ecommerce.service;

import java.util.List;

import com.train.proj.ecommerce.entity.Category;

public interface Categoryservice {
	public Category addCategory(Category category);
	public Category updateCategory(Category category);
	public void deleteCategory(Category category);
	public Category findById(Integer id);
	public List<Category> findAll();
}
