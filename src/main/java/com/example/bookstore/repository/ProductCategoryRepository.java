package com.example.bookstore.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bookstore.model.Category;
import com.example.bookstore.model.Product;
import com.example.bookstore.model.ProductCategory;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    Page<ProductCategory> findAll(Pageable pageable);
    Optional<ProductCategory> findByProductAndCategory(Product product, Category category);
    // Custom queries can be added here if needed
} 