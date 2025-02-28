package com.example.bookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bookstore.model.Cart;
import com.example.bookstore.model.CartDetail;
import com.example.bookstore.model.Product;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {
    List<CartDetail> findByCart(Cart cart);
    Optional<CartDetail> findByCartAndProduct(Cart cart, Product product);
} 