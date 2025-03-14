package com.example.bookstore.dto;

public class CartDetailDTO {
    private Long cartDetailId;
    private Long cartId;
    private Long productId;
    private String productName;
    private int originalPrice;  // Giá gốc
    private int discountedPrice; // Giá sau khuyến mãi
    private int discount;       // Phần trăm giảm giá
    private int quantity;
    private int subtotal;
    private String productImageUrl;

    // Constructors
    public CartDetailDTO() {}

    // Getters and Setters
    public int getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(int originalPrice) {
        this.originalPrice = originalPrice;
    }

    public int getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(int discountedPrice) {
        this.discountedPrice = discountedPrice;
        calculateSubtotal();
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public Long getCartDetailId() {
        return cartDetailId;
    }

    public void setCartDetailId(Long cartDetailId) {
        this.cartDetailId = cartDetailId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getPrice() {
        return discountedPrice; // Để tương thích ngược, price trả về giá sau khuyến mãi
    }

    public void setPrice(int price) {
        this.discountedPrice = price;
        calculateSubtotal();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateSubtotal();
    }

    public int getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(int subtotal) {
        this.subtotal = subtotal;
    }

    // Helper method
    private void calculateSubtotal() {
        this.subtotal = this.discountedPrice * this.quantity;
    }
} 