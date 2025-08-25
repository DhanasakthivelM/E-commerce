package com.train.proj.ecommerce.entity;
 
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
 
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "productId")
    private int productId;
   
    @Column(nullable = false, length = 100)
    private String name;
   
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
   
    @Column(nullable = false)
    private float price;
   
    @Column(nullable = false)
    private int stockQuantity;
   
    @Lob
	@Column(name = "imageData", columnDefinition = "MEDIUMBLOB")
	private byte[] imageData;
    
    // Many-to-One relationship with Category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId", nullable = false)
    private Category category;
   
    // One-to-Many relationship with Cart
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cart> cartItems;
   
    // Constructors
    public Product() {}
   
    public Product(String name, String description, float price, int stockQuantity, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }
   
    // Getters and Setters
    public int getProductId() {
        return productId;
    }
   
    public void setProductId(int productId) {
        this.productId = productId;
    }
   
    public String getName() {
        return name;
    }
   
    public void setName(String name) {
        this.name = name;
    }
   
    public String getDescription() {
        return description;
    }
   
    public void setDescription(String description) {
        this.description = description;
    }
   
    public float getPrice() {
        return price;
    }
   
    public void setPrice(float price) {
        this.price = price;
    }
   
    public int getStockQuantity() {
        return stockQuantity;
    }
   
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
   
    public Category getCategory() {
        return category;
    }
   
    public void setCategory(Category category) {
        this.category = category;
    }
   
    public List<Cart> getCartItems() {
        return cartItems;
    }
   
    public void setCartItems(List<Cart> cartItems) {
        this.cartItems = cartItems;
    }
   
    // Convenience method to get categoryId for backwards compatibility
    public int getCategoryId() {
        return category != null ? category.getCategoryId() : 0;
    }
   
    // Convenience method to get category name
    public String getCategoryName() {
        return category != null ? category.getName() : "Unknown";
    }
   
    // Convenience method to set categoryId for form binding
    public void setCategoryId(int categoryId) {
        // Create a minimal Category reference with just the ID for JPA
        if (categoryId > 0) {
            Category categoryRef = new Category();
            categoryRef.setCategoryId(categoryId);
            this.category = categoryRef;
        } else {
            this.category = null;
        }
    }
   
    public byte[] getImageData() {
        return imageData;
    }
   
    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}
 
 