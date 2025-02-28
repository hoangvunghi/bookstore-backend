package com.example.bookstore.dto;

import java.util.List;

public class CartDTO {
    private Long cartId;
    private Long userId;
    private List<CartDetailDTO> cartDetails;
    private int totalAmount;

    // Constructors
    public CartDTO() {}

    // Getters and Setters
    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartDetailDTO> getCartDetails() {
        return cartDetails;
    }

    public void setCartDetails(List<CartDetailDTO> cartDetails) {
        this.cartDetails = cartDetails;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }
} 