package com.example.bookstore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse> getProductCategoryById(@PathVariable Long id) {
        ProductCategoryDTO productCategory = productCategoryService.getProductCategoryById(id);
        if (productCategory != null) {
            return ResponseEntity.ok(new ApiResponse(true, "Lấy thông tin liên kết sản phẩm-danh mục thành công", productCategory));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createProductCategory(@RequestBody ProductCategoryDTO productCategoryDTO) {
        ProductCategoryDTO createdProductCategory = productCategoryService.createProductCategory(productCategoryDTO);
        if (createdProductCategory != null) {
            return ResponseEntity.ok(new ApiResponse(true, "Tạo liên kết sản phẩm-danh mục thành công", createdProductCategory));
        }
        return ResponseEntity.badRequest().body(new ApiResponse(false, "Không thể tạo liên kết sản phẩm-danh mục", null));
    }

    @DeleteMapping("/product/{productId}/category/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteProductCategory(
            @PathVariable Long productId,
            @PathVariable Long categoryId) {
        boolean deleted = productCategoryService.deleteProductCategory(productId, categoryId);
        if (deleted) {
            return ResponseEntity.ok(new ApiResponse(true, "Xóa liên kết sản phẩm-danh mục thành công", null));
        }
        return ResponseEntity.notFound().build();
    }
} 