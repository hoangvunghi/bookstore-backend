package com.example.bookstore.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageService productImageService;

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String errorMessage = "Dữ liệu JSON không hợp lệ";
        
        Throwable cause = ex.getCause();
        if (cause instanceof JsonParseException) {
            errorMessage = "Lỗi cú pháp JSON: " + cause.getMessage();
        } else if (cause instanceof JsonMappingException) {
            if (cause instanceof InvalidFormatException) {
                InvalidFormatException ife = (InvalidFormatException) cause;
                errorMessage = "Giá trị không hợp lệ cho trường '" + 
                    (ife.getPath().isEmpty() ? "không xác định" : ife.getPath().get(0).getFieldName()) + 
                    "'. Giá trị đúng phải là kiểu " + ife.getTargetType().getSimpleName();
            } else {
                errorMessage = "Lỗi ánh xếp JSON: " + cause.getMessage();
            }
        }
        
        return ResponseEntity.badRequest()
            .body(new ApiResponse(false, errorMessage, null));
    }

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
            System.out.println("Bắt đầu tạo sản phẩm mới");
            System.out.println("productID: " + productDTO.getProductId());
            
            // In ra thông tin về danh sách ảnh
            System.out.println("Số lượng ảnh nhận được: " + 
                (productDTO.getImageUrls() != null ? productDTO.getImageUrls().size() : 0));
            if (productDTO.getImageUrls() != null) {
                for (int i = 0; i < productDTO.getImageUrls().size(); i++) {
                    String imageData = productDTO.getImageUrls().get(i);
                    System.out.println("Ảnh thứ " + (i + 1) + ":");
                    if (imageData != null) {
                        System.out.println("- Độ dài dữ liệu: " + imageData.length());
                        System.out.println("- 100 ký tự đầu tiên: " + 
                            (imageData.length() > 100 ? imageData.substring(0, 100) + "..." : imageData));
                        System.out.println("- Có phải base64 không: " + 
                            (imageData.startsWith("data:image") || imageData.startsWith("data:application")));
                    } else {
                        System.out.println("- Dữ liệu null");
                    }
                }
            }

            if (productDTO.getProductId() != null && productDTO.getProductId() > 0) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, 
                        "Không thể tạo sản phẩm với ID đã được chỉ định. " +
                        "Để tạo sản phẩm mới, vui lòng không cung cấp productId. " +
                        "Để cập nhật sản phẩm, vui lòng sử dụng phương thức PUT /api/products/{id}", null));
            }
            
            if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Tên sản phẩm không được để trống", null));
            }
            
            if (productDTO.getPrice() <= 0) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Giá sản phẩm phải lớn hơn 0", null));
            }
            
            if (productDTO.getStockQuantity() < 0) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Số lượng tồn kho không được âm", null));
            }
            
            if (productDTO.getDiscount() < 0 || productDTO.getDiscount() > 100) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Giảm giá phải từ 0 đến 100%", null));
            }
            
            if (productDTO.getPublicationYear() < 0 || productDTO.getPublicationYear() > 9999) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Năm xuất bản không hợp lệ", null));
            }
            
            // Xử lý hình ảnh base64 trước khi tạo sản phẩm
            List<String> processedImageUrls = new ArrayList<>();
            if (productDTO.getImageUrls() != null && !productDTO.getImageUrls().isEmpty()) {
                System.out.println("Bắt đầu xử lý " + productDTO.getImageUrls().size() + " ảnh");
                for (String imageData : productDTO.getImageUrls()) {
                    try {
                        // Kiểm tra xem có phải là base64 không
                        if (imageData != null && !imageData.trim().isEmpty()) {
                            System.out.println("Đang xử lý ảnh với độ dài: " + imageData.length());
                            if (imageData.startsWith("data:image") || imageData.startsWith("data:application")) {
                                System.out.println("Phát hiện ảnh base64, đang tải lên Cloudinary");
                                // Nếu là base64, tải lên Cloudinary
                                String imageUrl = productImageService.addProductImage(null, imageData).getImageURL();
                                System.out.println("Đã tải lên thành công, URL: " + imageUrl);
                                processedImageUrls.add(imageUrl);
                            } else {
                                System.out.println("Không phải base64, giữ nguyên URL");
                                // Nếu không phải base64, giữ nguyên URL
                                processedImageUrls.add(imageData);
                            }
                        } else {
                            System.out.println("Bỏ qua ảnh rỗng hoặc null");
                        }
                    } catch (Exception e) {
                        System.err.println("Lỗi xử lý hình ảnh: " + e.getMessage());
                        e.printStackTrace();
                        // Nếu có lỗi, bỏ qua hình ảnh này
                    }
                }
                System.out.println("Đã xử lý xong " + processedImageUrls.size() + " ảnh");
                // Cập nhật danh sách hình ảnh đã xử lý
                productDTO.setImageUrls(processedImageUrls);
            }
            
            ProductDTO createdProduct = productService.createProduct(productDTO);
            System.out.println("Đã tạo sản phẩm thành công với ID: " + createdProduct.getProductId());
            return ResponseEntity.ok(new ApiResponse(true, "Tạo sản phẩm thành công", createdProduct));
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo sản phẩm: " + e.getMessage());
            e.printStackTrace();
            String errorMessage = e.getMessage();
            
            if (errorMessage != null && errorMessage.contains("Row was updated or deleted by another transaction")) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, 
                        "Không thể tạo sản phẩm với ID đã tồn tại. " +
                        "Để tạo sản phẩm mới, vui lòng không cung cấp productId. " +
                        "Để cập nhật sản phẩm, vui lòng sử dụng phương thức PUT /api/products/{id}", null));
            }
            
            if (errorMessage != null && errorMessage.contains("duplicate key value violates unique constraint \"productcategory_pkey\"")) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, 
                        "Lỗi khi liên kết sản phẩm với danh mục. Có thể có vấn đề với khóa chính trong bảng liên kết. " +
                        "Vui lòng liên hệ quản trị viên để được hỗ trợ.", null));
            }
            
            if (errorMessage != null && errorMessage.contains("violates foreign key constraint")) {
                if (errorMessage.contains("categoryids")) {
                    return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Một hoặc nhiều danh mục không tồn tại. Vui lòng kiểm tra lại danh sách categoryIds.", null));
                }
            }
            
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tạo sản phẩm thất bại: " + errorMessage, null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDTO productDTO) {
        try {
            if (productDTO.getName() != null && productDTO.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Tên sản phẩm không được để trống", null));
            }
            
            if (productDTO.getPrice() <= 0) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Giá sản phẩm phải lớn hơn 0", null));
            }
            
            if (productDTO.getStockQuantity() < 0) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Số lượng tồn kho không được âm", null));
            }
            
            if (productDTO.getDiscount() < 0 || productDTO.getDiscount() > 100) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Giảm giá phải từ 0 đến 100%", null));
            }
            
            if (productDTO.getPublicationYear() < 0 || productDTO.getPublicationYear() > 9999) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Năm xuất bản không hợp lệ", null));
            }
            
            // Xử lý hình ảnh base64 trước khi cập nhật sản phẩm
            List<String> processedImageUrls = new ArrayList<>();
            if (productDTO.getImageUrls() != null && !productDTO.getImageUrls().isEmpty()) {
                for (String imageData : productDTO.getImageUrls()) {
                    try {
                        // Kiểm tra xem có phải là base64 không
                        if (imageData != null && !imageData.trim().isEmpty()) {
                            if (imageData.startsWith("data:image") || imageData.startsWith("data:application")) {
                                // Nếu là base64, tải lên Cloudinary
                                String imageUrl = productImageService.addProductImage(id, imageData).getImageURL();
                                processedImageUrls.add(imageUrl);
                            } else {
                                // Nếu không phải base64, giữ nguyên URL
                                processedImageUrls.add(imageData);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Lỗi xử lý hình ảnh: " + e.getMessage());
                        // Nếu có lỗi, bỏ qua hình ảnh này
                    }
                }
                // Cập nhật danh sách hình ảnh đã xử lý
                productDTO.setImageUrls(processedImageUrls);
            }
            
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
            @RequestParam(required = false) Integer minRealPrice,
            @RequestParam(required = false) Integer maxRealPrice,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Integer minSold,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false, defaultValue = "product_id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            System.out.println("==== CONTROLLER PARAMS ====");
            System.out.println("Controller received sortBy: " + sortBy);
            System.out.println("Controller received sortDir: " + sortDir);
            System.out.println("Controller received pageable: " + pageable);
            System.out.println("============================");
            
            Page<ProductDTO> products = productService.advancedSearch(
                    name, author, publisher, minPrice, maxPrice, minRealPrice, maxRealPrice,
                    year, categoryId, minStock, minSold, minRating, sortBy, sortDir, pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Tìm kiếm nâng cao thành công", products));
        } catch (PropertyReferenceException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ: " + e.getMessage(), null));
        } catch (Exception e) {
            System.out.println("==== ERROR DETAILS ====");
            System.out.println("Error in search: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=======================");
            
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

    @GetMapping("/best-selling")
    public ResponseEntity<ApiResponse> getBestSellingProducts(
            @RequestParam(defaultValue = "0") int minSold,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<ProductDTO> products = productService.getBestSellingProducts(minSold, pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách sản phẩm bán chạy thành công", products));
        } catch (PropertyReferenceException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Lấy danh sách sản phẩm bán chạy thất bại: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/top-selling")
    public ResponseEntity<ApiResponse> getTopSellingProducts(
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<ProductDTO> products = productService.getTopSellingProducts(pageable);
            return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách sản phẩm bán chạy nhất thành công", products));
        } catch (PropertyReferenceException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Lấy danh sách sản phẩm bán chạy nhất thất bại: " + e.getMessage(), null));
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

    // API lấy danh sách sản phẩm có tồn kho thấp (chỉ ADMIN)
    @GetMapping("/low-inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getLowInventoryProducts(
            @RequestParam(defaultValue = "5") int threshold) {
        List<ProductDTO> products = productService.getLowInventoryProducts(threshold);
        return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách sản phẩm tồn kho thấp thành công", products));
    }

    @GetMapping("/test-sort")
    public ResponseEntity<ApiResponse> testSorting(
            @RequestParam(required = false, defaultValue = "product_id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            System.out.println("==== TEST SORT PARAMS ====");
            System.out.println("Test received sortBy: " + sortBy);
            System.out.println("Test received sortDir: " + sortDir);
            System.out.println("Original pageable: " + pageable);
            
            // Tạo sort mới
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            
            String dbField = sortBy;
            if (!sortBy.contains("_") && !"name".equals(sortBy) && !"price".equals(sortBy) 
                && !"newest".equals(sortBy) && !"bestselling".equals(sortBy) 
                && !"bestseller".equals(sortBy) && !"soldCount".equals(sortBy)) {
                // Chuyển đổi camelCase sang snake_case
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < sortBy.length(); i++) {
                    char c = sortBy.charAt(i);
                    if (Character.isUpperCase(c)) {
                        result.append('_');
                        result.append(Character.toLowerCase(c));
                    } else {
                        result.append(c);
                    }
                }
                dbField = result.toString();
            } else if ("price".equals(sortBy)) {
                dbField = "real_price"; // Sửa lại thành "real_price" để khớp với database
            } else if ("newest".equals(sortBy)) {
                dbField = "product_id"; // Sửa thành product_id thay vì publication_year
            } else if ("bestselling".equals(sortBy) || "bestseller".equals(sortBy) || "soldCount".equals(sortBy)) {
                dbField = "sold_count"; // Sửa lại thành "sold_count" để khớp với database
            }
            
            System.out.println("Final dbField: " + dbField);
            System.out.println("Final direction: " + direction);
            
            // Tạo pageable mới
            PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                direction, 
                dbField);
            
            System.out.println("Generated pageRequest: " + pageRequest);
            System.out.println("==========================");
            
            // Sử dụng phương thức đơn giản - chỉ lấy danh sách chính
            Page<ProductDTO> products = productService.getAllProducts(pageRequest);
            
            return ResponseEntity.ok(new ApiResponse(true, "Test sắp xếp thành công", products));
        } catch (PropertyReferenceException e) {
            System.out.println("PropertyReferenceException: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Tham số sắp xếp không hợp lệ: " + e.getMessage(), null));
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Test thất bại: " + e.getMessage(), null));
        }
    }
}