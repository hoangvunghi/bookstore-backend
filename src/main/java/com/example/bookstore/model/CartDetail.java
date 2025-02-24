package com.example.bookstore.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cartdetails")
public class CartDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartDetailId;

    @ManyToOne
    @JoinColumn(name = "cartId")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "productId")
    private Product product;

    private int quantity;

    // Getters, setters, constructors
    public CartDetail() {}

    public Long getCartDetailId() { return cartDetailId; }
    public void setCartDetailId(Long cartDetailId) { this.cartDetailId = cartDetailId; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}