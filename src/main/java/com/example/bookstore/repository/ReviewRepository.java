package com.example.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bookstore.model.Order;
import com.example.bookstore.model.Product;
import com.example.bookstore.model.Review;
import com.example.bookstore.model.User;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUserAndProductAndOrder(User user, Product product, Order order);
} 