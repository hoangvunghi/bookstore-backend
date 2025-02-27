package com.example.bookstore.dto;

public class ProductCategoryDTO {
    private Long productCategoryId;
    private Long productId;
    private Long categoryId;

    public ProductCategoryDTO() {}

    public Long getProductCategoryId() { return productCategoryId; }
    public void setProductCategoryId(Long productCategoryId) { this.productCategoryId = productCategoryId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
} 