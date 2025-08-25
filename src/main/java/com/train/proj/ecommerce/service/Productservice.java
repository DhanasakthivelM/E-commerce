package com.train.proj.ecommerce.service;

import java.util.List;

import com.train.proj.ecommerce.entity.Product;

public interface Productservice  {
	
	public Product addProduct(Product product);
	public Product updateProduct(Product product);
	public void deleteProduct(Product product);
	public Product findById(Integer id);
	public List<Product> findAll();
}
