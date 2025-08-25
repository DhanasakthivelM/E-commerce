package com.train.proj.ecommerce.controller;


import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.train.proj.ecommerce.entity.Product;
import com.train.proj.ecommerce.repository.CategoryRepository;
import com.train.proj.ecommerce.repository.ProductRepository;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;


    public ProductController(ProductRepository productRepository,
            CategoryRepository categoryRepository) {
		this.productRepository = productRepository;
    }

    @GetMapping("/product-image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getProductImage(@PathVariable Integer id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent() && optionalProduct.get().getImageData() != null) {
            byte[] imageData = optionalProduct.get().getImageData();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // or detect dynamically if needed
            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/allproducts")
    public String getAllProducts(Model model) {
        List<Product> allproducts=productRepository.findAll();
        model.addAttribute("allproducts",allproducts);
        return "allproducts-form";
    }
    
    @GetMapping("/viewproduct/{id}")
    public String showproductdetails(@PathVariable Integer id, Model model) {
        Optional<Product> optionalProduct = productRepository.findByIdWithCategory(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            model.addAttribute("product", product);
            return "viewproductpage";
        } else {
            // Product not found, redirect to all products page
            model.addAttribute("error", "Product not found");
            return "redirect:/products/allproducts";
        }
    } 
    
    @GetMapping("/search")
    public String searchedProducts(@RequestParam("query") String query,Model model){
    	List<Product> allproducts=productRepository.findByNameContaining(query);
    	model.addAttribute("allproducts",allproducts);
    	return "allproducts-form";
    }
}
