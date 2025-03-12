package com.example.bookstore.dto;

import java.util.List;

public class ProductDTO {
    private Long productId;
    private String name;
    private String description;
    private int price;
    private int stockQuantity;
    private int discount;
    private int realPrice;
    private String author;
    private String publisher;
    private int publicationYear;
    private int pageCount;
    private String ISBN;
    private int soldCount;
    private List<Long> categoryIds;
    private List<String> imageUrls;
    private double averageRating;
    private int reviewCount;
    private boolean isActive;

    public ProductDTO() {}

    // Getters and setters
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
    public List<Long> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<Long> categoryIds) { this.categoryIds = categoryIds; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
} 