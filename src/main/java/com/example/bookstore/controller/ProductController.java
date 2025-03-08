package com.example.bookstore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PropertyReferenceException;
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

import com.example.bookstore.dto.ApiResponse;
import com.example.bookstore.dto.ProductDTO;
import com.example.bookstore.dto.ProductImageDTO;
import com.example.bookstore.service.ProductImageService;
import com.example.bookstore.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageService productImageService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllProducts(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        try {
            Page<ProductDTO> products = productService.getAllProducts(pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách sản phẩm thành công", products));
        } catch (PropertyReferenceException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ. Vui lòng sử dụng các thuộc tính hợp lệ như: productId, name, price, etc.", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Đã xảy ra lỗi khi lấy danh sách sản phẩm: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long id) {
        try {
            ProductDTO product = productService.getProductById(id);
            if (product == null) {
                return ResponseEntity.notFound()
                    .build();
            }
            return ResponseEntity.ok(new ApiResponse(true, "Lấy thông tin sản phẩm thành công", product));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Đã xảy ra lỗi khi lấy thông tin sản phẩm: " + e.getMessage(), null));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createProduct(@RequestBody ProductDTO productDTO) {
        try {
            ProductDTO createdProduct = productService.createProduct(productDTO);
            return ResponseEntity.ok(new ApiResponse(true, "Tạo sản phẩm thành công", createdProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tạo sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            if (updatedProduct == null) {
                return ResponseEntity.notFound()
                    .build();
            }
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật sản phẩm thành công", updatedProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Cập nhật sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        try {
            if (productService.deleteProduct(id)) {
                return ResponseEntity.ok(new ApiResponse(true, "Xóa sản phẩm thành công", null));
            }
            return ResponseEntity.notFound()
                .build();
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Xóa sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse> searchByName(
            @RequestParam String name,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<ProductDTO> products = productService.searchByName(name, pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Tìm kiếm sản phẩm theo tên thành công", products));
        } catch (PropertyReferenceException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Tìm kiếm sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @GetMapping("/search/author")
    public ResponseEntity<ApiResponse> searchByAuthor(
            @RequestParam String author,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<ProductDTO> products = productService.searchByAuthor(author, pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Tìm kiếm sản phẩm theo tác giả thành công", products));
        } catch (PropertyReferenceException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Tìm kiếm sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @GetMapping("/search/publisher")
    public ResponseEntity<ApiResponse> searchByPublisher(
            @RequestParam String publisher,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<ProductDTO> products = productService.searchByPublisher(publisher, pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Tìm kiếm sản phẩm theo nhà xuất bản thành công", products));
        } catch (PropertyReferenceException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Tìm kiếm sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @GetMapping("/search/isbn")
    public ResponseEntity<ApiResponse> searchByISBN(@RequestParam String isbn) {
        try {
            ProductDTO product = productService.searchByISBN(isbn);
            if (product == null) {
                return ResponseEntity.notFound()
                    .build();
            }
            return ResponseEntity.ok(new ApiResponse(true, "Tìm kiếm sản phẩm theo ISBN thành công", product));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Tìm kiếm sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @GetMapping("/search/price-range")
    public ResponseEntity<ApiResponse> searchByPriceRange(
            @RequestParam int minPrice,
            @RequestParam int maxPrice,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<ProductDTO> products = productService.searchByPriceRange(minPrice, maxPrice, pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Tìm kiếm sản phẩm theo khoảng giá thành công", products));
        } catch (PropertyReferenceException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Tìm kiếm sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @GetMapping("/search/year")
    public ResponseEntity<ApiResponse> searchByPublicationYear(
            @RequestParam int year,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<ProductDTO> products = productService.searchByPublicationYear(year, pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Tìm kiếm sản phẩm theo năm xuất bản thành công", products));
        } catch (PropertyReferenceException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Tìm kiếm sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @GetMapping("/search/category/{categoryId}")
    public ResponseEntity<ApiResponse> searchByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<ProductDTO> products = productService.searchByCategory(categoryId, pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Tìm kiếm sản phẩm theo danh mục thành công", products));
        } catch (PropertyReferenceException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Tìm kiếm sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @GetMapping("/search/advanced")
    public ResponseEntity<ApiResponse> advancedSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<ProductDTO> products = productService.advancedSearch(
                    name, author, publisher, minPrice, maxPrice, year, categoryId, pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Tìm kiếm nâng cao thành công", products));
        } catch (PropertyReferenceException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Tìm kiếm sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @GetMapping("/discounted")
    public ResponseEntity<ApiResponse> getDiscountedProducts(
            @RequestParam(defaultValue = "0") int minDiscount,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<ProductDTO> products = productService.getDiscountedProducts(minDiscount, pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách sản phẩm giảm giá thành công", products));
        } catch (PropertyReferenceException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Lấy danh sách sản phẩm giảm giá thất bại: " + e.getMessage(), null));
        }
    }

    @GetMapping("/new")
    public ResponseEntity<ApiResponse> getNewProducts(
            @RequestParam int year,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<ProductDTO> products = productService.getNewProducts(year, pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách sản phẩm mới thành công", products));
        } catch (PropertyReferenceException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Lấy danh sách sản phẩm mới thất bại: " + e.getMessage(), null));
        }
    }

    @GetMapping("/in-stock")
    public ResponseEntity<ApiResponse> getInStockProducts(
            @RequestParam(defaultValue = "0") int minStock,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<ProductDTO> products = productService.getInStockProducts(minStock, pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách sản phẩm còn hàng thành công", products));
        } catch (PropertyReferenceException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Lấy danh sách sản phẩm còn hàng thất bại: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{productId}/images")
    public ResponseEntity<ApiResponse> getProductImages(@PathVariable Long productId) {
        try {
            List<ProductImageDTO> images = productImageService.getProductImages(productId);
            if (images == null) {
                return ResponseEntity.notFound()
                    .build();
            }
            return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách ảnh sản phẩm thành công", images));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Lấy danh sách ảnh sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @PostMapping("/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> addProductImage(
            @PathVariable Long productId,
            @RequestParam String imageURL) {
        try {
            ProductImageDTO image = productImageService.addProductImage(productId, imageURL);
            if (image == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Thêm ảnh sản phẩm thất bại", null));
            }
            return ResponseEntity.ok(new ApiResponse(true, "Thêm ảnh sản phẩm thành công", image));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Thêm ảnh sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @PutMapping("/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateProductImage(
            @PathVariable Long imageId,
            @RequestParam String imageURL) {
        try {
            ProductImageDTO image = productImageService.updateProductImage(imageId, imageURL);
            if (image == null) {
                return ResponseEntity.notFound()
                    .build();
            }
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật ảnh sản phẩm thành công", image));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Cập nhật ảnh sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteProductImage(@PathVariable Long imageId) {
        try {
            if (productImageService.deleteProductImage(imageId)) {
                return ResponseEntity.ok(new ApiResponse(true, "Xóa ảnh sản phẩm thành công", null));
            }
            return ResponseEntity.notFound()
                .build();
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Xóa ảnh sản phẩm thất bại: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteAllProductImages(@PathVariable Long productId) {
        try {
            productImageService.deleteAllProductImages(productId);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa tất cả ảnh sản phẩm thành công", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Xóa tất cả ảnh sản phẩm thất bại: " + e.getMessage(), null));
        }
    }
}