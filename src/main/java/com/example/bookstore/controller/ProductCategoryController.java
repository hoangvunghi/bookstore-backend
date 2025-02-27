package com.example.bookstore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.ProductCategoryDTO;
import com.example.bookstore.service.ProductCategoryService;

@RestController
@RequestMapping("/api/product-categories")
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService productCategoryService;

    @GetMapping
    public ResponseEntity<Page<ProductCategoryDTO>> getAllProductCategories(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<ProductCategoryDTO> productCategories = productCategoryService.getAllProductCategories(pageable);
        return ResponseEntity.ok(productCategories);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductCategoryDTO>> getAllProductCategoriesWithoutPaging() {
        List<ProductCategoryDTO> productCategories = productCategoryService.getAllProductCategories();
        return ResponseEntity.ok(productCategories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategoryDTO> getProductCategoryById(@PathVariable Long id) {
        ProductCategoryDTO productCategory = productCategoryService.getProductCategoryById(id);
        if (productCategory != null) {
            return ResponseEntity.ok(productCategory);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ProductCategoryDTO> createProductCategory(@RequestBody ProductCategoryDTO productCategoryDTO) {
        ProductCategoryDTO createdProductCategory = productCategoryService.createProductCategory(productCategoryDTO);
        return ResponseEntity.ok(createdProductCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCategoryDTO> updateProductCategory(
            @PathVariable Long id,
            @RequestBody ProductCategoryDTO productCategoryDTO) {
        ProductCategoryDTO updatedProductCategory = productCategoryService.updateProductCategory(id, productCategoryDTO);
        if (updatedProductCategory != null) {
            return ResponseEntity.ok(updatedProductCategory);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductCategory(@PathVariable Long id) {
        boolean deleted = productCategoryService.deleteProductCategory(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
} 