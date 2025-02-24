package com.example.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookstore.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}