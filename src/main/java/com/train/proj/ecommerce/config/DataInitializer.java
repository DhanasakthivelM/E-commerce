package com.train.proj.ecommerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.train.proj.ecommerce.entity.Category;
import com.train.proj.ecommerce.repository.CategoryRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if categories already exist to avoid duplicates
        if (categoryRepository.count() == 0) {
            // Create sample categories
            Category electronics = new Category("Electronics");
            Category clothing = new Category("Clothing");
            Category books = new Category("Books");
            Category home = new Category("Home & Garden");
            Category sports = new Category("Sports");
            Category automotive = new Category("Automotive");
            
            // Save categories
            categoryRepository.save(electronics);
            categoryRepository.save(clothing);
            categoryRepository.save(books);
            categoryRepository.save(home);
            categoryRepository.save(sports);
            categoryRepository.save(automotive);
            
            System.out.println("Sample categories created successfully!");
        } else {
            System.out.println("Categories already exist, skipping initialization.");
        }
    }
} 