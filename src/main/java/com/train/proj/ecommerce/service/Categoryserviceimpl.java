package com.train.proj.ecommerce.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.train.proj.ecommerce.entity.Category;
import com.train.proj.ecommerce.repository.CategoryRepository;

@Service
public class Categoryserviceimpl implements Categoryservice {
	@Autowired
	private CategoryRepository categoryRepository;

	@Override
	public Category addCategory(Category category) {
		return categoryRepository.save(category);
		
	}

	@Override
	public Category updateCategory(Category category) {
		return categoryRepository.save(category);
	}

	@Override
	public void deleteCategory(Category category) {
		categoryRepository.delete(category);

	}

	@Override
	public Category findById(Integer id) {
		return categoryRepository.findById(id).get();
	}

	@Override
	public List<Category> findAll() {
		return categoryRepository.findAll();
	}

}
