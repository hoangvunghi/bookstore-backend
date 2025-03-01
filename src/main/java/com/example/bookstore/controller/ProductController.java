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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.ProductDTO;
import com.example.bookstore.dto.ProductImageDTO;
import com.example.bookstore.dto.ApiResponse;
import com.example.bookstore.service.ProductImageService;
import com.example.bookstore.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageService productImageService;

    // API lấy tất cả sản phẩm có phân trang
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    // API lấy sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }

    // API tạo sản phẩm mới
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.ok(new ApiResponse(true, "Tạo sản phẩm thành công", createdProduct));
    }

    // API cập nhật sản phẩm
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDTO productDTO) {
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        if (updatedProduct != null) {
            return ResponseEntity.ok(updatedProduct);
        }
        return ResponseEntity.notFound().build();
    }

    // API xóa sản phẩm
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        if (productService.deleteProduct(id)) {
            return ResponseEntity.ok(new ApiResponse(true, "Xóa sản phẩm thành công"));
        }
        return ResponseEntity.notFound().build();
    }

    // API tìm kiếm theo tên
    @GetMapping("/search/name")
    public ResponseEntity<Page<ProductDTO>> searchByName(
            @RequestParam String name,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.searchByName(name, pageable));
    }

    // API tìm kiếm theo tác giả
    @GetMapping("/search/author")
    public ResponseEntity<Page<ProductDTO>> searchByAuthor(
            @RequestParam String author,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.searchByAuthor(author, pageable));
    }

    // API tìm kiếm theo nhà xuất bản
    @GetMapping("/search/publisher")
    public ResponseEntity<Page<ProductDTO>> searchByPublisher(
            @RequestParam String publisher,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.searchByPublisher(publisher, pageable));
    }

    // API tìm kiếm theo ISBN
    @GetMapping("/search/isbn")
    public ResponseEntity<ProductDTO> searchByISBN(@RequestParam String isbn) {
        ProductDTO product = productService.searchByISBN(isbn);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }

    // API tìm kiếm theo khoảng giá
    @GetMapping("/search/price-range")
    public ResponseEntity<Page<ProductDTO>> searchByPriceRange(
            @RequestParam int minPrice,
            @RequestParam int maxPrice,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.searchByPriceRange(minPrice, maxPrice, pageable));
    }

    // API tìm kiếm theo năm xuất bản
    @GetMapping("/search/year")
    public ResponseEntity<Page<ProductDTO>> searchByPublicationYear(
            @RequestParam int year,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.searchByPublicationYear(year, pageable));
    }

    // API tìm kiếm theo danh mục
    @GetMapping("/search/category/{categoryId}")
    public ResponseEntity<Page<ProductDTO>> searchByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.searchByCategory(categoryId, pageable));
    }

    // API tìm kiếm nâng cao
    @GetMapping("/search/advanced")
    public ResponseEntity<Page<ProductDTO>> advancedSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.advancedSearch(
                name, author, publisher, minPrice, maxPrice, year, categoryId, pageable));
    }

    // API lấy sản phẩm đang giảm giá
    @GetMapping("/discounted")
    public ResponseEntity<Page<ProductDTO>> getDiscountedProducts(
            @RequestParam(defaultValue = "0") int minDiscount,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.getDiscountedProducts(minDiscount, pageable));
    }

    // API lấy sản phẩm mới
    @GetMapping("/new")
    public ResponseEntity<Page<ProductDTO>> getNewProducts(
            @RequestParam int year,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.getNewProducts(year, pageable));
    }

    // API lấy sản phẩm còn hàng
    @GetMapping("/in-stock")
    public ResponseEntity<Page<ProductDTO>> getInStockProducts(
            @RequestParam(defaultValue = "0") int minStock,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.getInStockProducts(minStock, pageable));
    }

    // API lấy danh sách ảnh của sản phẩm
    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductImageDTO>> getProductImages(@PathVariable Long productId) {
        List<ProductImageDTO> images = productImageService.getProductImages(productId);
        if (images == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(images);
    }

    // API thêm ảnh cho sản phẩm (chỉ ADMIN)
    @PostMapping("/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> addProductImage(
            @PathVariable Long productId,
            @RequestParam String imageURL) {
        ProductImageDTO image = productImageService.addProductImage(productId, imageURL);
        if (image == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(new ApiResponse(true, "Thêm ảnh sản phẩm thành công", image));
    }

    // API cập nhật ảnh sản phẩm (chỉ ADMIN)
    @PutMapping("/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductImageDTO> updateProductImage(
            @PathVariable Long imageId,
            @RequestParam String imageURL) {
        ProductImageDTO image = productImageService.updateProductImage(imageId, imageURL);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(image);
    }

    // API xóa ảnh sản phẩm (chỉ ADMIN)
    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteProductImage(@PathVariable Long imageId) {
        boolean deleted = productImageService.deleteProductImage(imageId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponse(true, "Xóa ảnh sản phẩm thành công"));
    }

    // API xóa tất cả ảnh của sản phẩm (chỉ ADMIN)
    @DeleteMapping("/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteAllProductImages(@PathVariable Long productId) {
        productImageService.deleteAllProductImages(productId);
        return ResponseEntity.ok(new ApiResponse(true, "Xóa tất cả ảnh sản phẩm thành công"));
    }
}