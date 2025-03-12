package com.example.bookstore.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    private int price;
    private int stockQuantity;
    private int discount;
    private int realPrice;
    @Column(name = "author", columnDefinition = "VARCHAR(255)")
    private String author;
    @Column(name = "publisher", columnDefinition = "VARCHAR(255)")
    private String publisher;
    private int publicationYear;
    private int pageCount;
    private String ISBN;
    private int soldCount;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT true")
    private boolean isActive = true;

    @OneToMany(mappedBy = "product")
    private List<Review> reviews;

    @OneToMany(mappedBy = "product")
    private List<CartDetail> cartDetails;

    @OneToMany(mappedBy = "product")
    private List<OrderDetail> orderDetails;

    @ManyToMany
    @JoinTable(
        name = "productcategory",
        joinColumns = @JoinColumn(name = "productId"),
        inverseJoinColumns = @JoinColumn(name = "categoryId")
    )
    private List<Category> categories;

    @OneToMany(mappedBy = "product")
    private List<ProductImage> productImages;

    // Getters, setters, constructors
    public Product() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }
    public int getRealPrice() { return realPrice; }
    public void setRealPrice(int realPrice) { this.realPrice = realPrice; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public int getPublicationYear() { return publicationYear; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }
    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }
    public String getISBN() { return ISBN; }
    public void setISBN(String ISBN) { this.ISBN = ISBN; }
    public int getSoldCount() { return soldCount; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    public List<CartDetail> getCartDetails() { return cartDetails; }
    public void setCartDetails(List<CartDetail> cartDetails) { this.cartDetails = cartDetails; }
    public List<OrderDetail> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetail> orderDetails) { this.orderDetails = orderDetails; }
    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
    public List<ProductImage> getProductImages() { return productImages; }
    public void setProductImages(List<ProductImage> productImages) { this.productImages = productImages; }
}