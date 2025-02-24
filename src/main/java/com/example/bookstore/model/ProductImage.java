package com.example.bookstore.model;

import jakarta.persistence.*;

@Entity
@Table(name = "productimage")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productImageId;

    @ManyToOne
    @JoinColumn(name = "productId")
    private Product product;

    private String imageURL;  // Base64 encoded image

    // Getters, setters, constructors
    public ProductImage() {}

    public Long getProductImageId() { return productImageId; }
    public void setProductImageId(Long productImageId) { this.productImageId = productImageId; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
}