package com.example.bookstore.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.dto.ProductDTO;
import com.example.bookstore.model.Category;
import com.example.bookstore.model.Product;
import com.example.bookstore.model.ProductImage;
import com.example.bookstore.model.Review;
import com.example.bookstore.repository.CategoryRepository;
import com.example.bookstore.repository.ProductImageRepository;
import com.example.bookstore.repository.ProductRepository;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductImageRepository productImageRepository;

    // Phương thức lấy tất cả sản phẩm có phân trang
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable).map(this::convertToDTO);
    }

    // Phương thức lấy sản phẩm theo ID
    public ProductDTO getProductById(Long id) {
        Optional<Product> product = productRepository.findByProductIdAndIsActiveTrue(id);
        return product.map(this::convertToDTO).orElse(null);
    }

    // Phương thức tạo sản phẩm mới
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = convertToEntity(productDTO);
        product.setActive(true);  // Đảm bảo sản phẩm mới luôn active
        Product savedProduct = productRepository.save(product);
        
        // Lưu hình ảnh sản phẩm
        if (productDTO.getImageUrls() != null) {
            for (String imageUrl : productDTO.getImageUrls()) {
                ProductImage image = new ProductImage();
                image.setProduct(savedProduct);
                image.setImageURL(imageUrl);
                productImageRepository.save(image);
            }
        }
        
        return convertToDTO(savedProduct);
    }

    // Phương thức cập nhật sản phẩm
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        if (productRepository.existsById(id)) {
            Product product = convertToEntity(productDTO);
            product.setProductId(id);
            Product updatedProduct = productRepository.save(product);
            
            // Cập nhật hình ảnh sản phẩm
            productImageRepository.deleteByProduct(updatedProduct);
            if (productDTO.getImageUrls() != null) {
                for (String imageUrl : productDTO.getImageUrls()) {
                    ProductImage image = new ProductImage();
                    image.setProduct(updatedProduct);
                    image.setImageURL(imageUrl);
                    productImageRepository.save(image);
                }
            }
            
            return convertToDTO(updatedProduct);
        }
        return null;
    }

    // Phương thức xóa sản phẩm (soft delete)
    @Transactional
    public boolean deleteProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setActive(false);  // Soft delete
            productRepository.save(product);
            return true;
        }
        return false;
    }

    // Các phương thức tìm kiếm
    public Page<ProductDTO> searchByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name, pageable)
                .map(this::convertToDTO);
    }

    public Page<ProductDTO> searchByAuthor(String author, Pageable pageable) {
        return productRepository.findByAuthorContainingIgnoreCaseAndIsActiveTrue(author, pageable)
                .map(this::convertToDTO);
    }

    public Page<ProductDTO> searchByPublisher(String publisher, Pageable pageable) {
        return productRepository.findByPublisherContainingIgnoreCaseAndIsActiveTrue(publisher, pageable)
                .map(this::convertToDTO);
    }

    public ProductDTO searchByISBN(String isbn) {
        Product product = productRepository.findByISBN(isbn);
        return product != null ? convertToDTO(product) : null;
    }

    public Page<ProductDTO> searchByPriceRange(int minPrice, int maxPrice, Pageable pageable) {
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable)
                .map(this::convertToDTO);
    }

    public Page<ProductDTO> searchByPublicationYear(int year, Pageable pageable) {
        return productRepository.findByPublicationYear(year, pageable)
                .map(this::convertToDTO);
    }

    public Page<ProductDTO> searchByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(this::convertToDTO);
    }

    public Page<ProductDTO> advancedSearch(
            String name, 
            String author, 
            String publisher, 
            Integer minPrice, 
            Integer maxPrice, 
            Integer year, 
            Long categoryId, 
            Pageable pageable) {
        return productRepository.searchProducts(
                name, author, publisher, minPrice, maxPrice, year, categoryId, pageable)
                .map(this::convertToDTO);
    }

    // Các phương thức đặc biệt
    public Page<ProductDTO> getDiscountedProducts(int minDiscount, Pageable pageable) {
        return productRepository.findByDiscountGreaterThan(minDiscount, pageable)
                .map(this::convertToDTO);
    }

    public Page<ProductDTO> getNewProducts(int year, Pageable pageable) {
        return productRepository.findByPublicationYearOrderByPublicationYearDesc(year, pageable)
                .map(this::convertToDTO);
    }

    public Page<ProductDTO> getInStockProducts(int minStock, Pageable pageable) {
        return productRepository.findByStockQuantityGreaterThan(minStock, pageable)
                .map(this::convertToDTO);
    }

    public Page<ProductDTO> getBestSellingProducts(int minSold, Pageable pageable) {
        return productRepository.findBySoldCountGreaterThanOrderBySoldCountDesc(minSold, pageable)
                .map(this::convertToDTO);
    }

    public Page<ProductDTO> getTopSellingProducts(Pageable pageable) {
        return productRepository.findTopSellingProducts(pageable)
                .map(this::convertToDTO);
    }

    // Phương thức chuyển đổi Entity sang DTO
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setDiscount(product.getDiscount());
        dto.setRealPrice(product.getRealPrice());
        dto.setAuthor(product.getAuthor());
        dto.setPublisher(product.getPublisher());
        dto.setPublicationYear(product.getPublicationYear());
        dto.setPageCount(product.getPageCount());
        dto.setISBN(product.getISBN());
        dto.setSoldCount(product.getSoldCount());
        dto.setActive(product.isActive());  // Thêm trường isActive vào DTO
        
        // Chuyển đổi danh sách category IDs
        if (product.getCategories() != null) {
            dto.setCategoryIds(product.getCategories().stream()
                    .map(Category::getCategoryId)
                    .collect(Collectors.toList()));
        }
        
        // Chuyển đổi danh sách image URLs
        if (product.getProductImages() != null) {
            dto.setImageUrls(product.getProductImages().stream()
                    .map(ProductImage::getImageURL)
                    .collect(Collectors.toList()));
        }
        
        // Tính toán đánh giá trung bình và số lượng đánh giá
        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            double avgRating = product.getReviews().stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            dto.setAverageRating(avgRating);
            dto.setReviewCount(product.getReviews().size());
        }
        
        return dto;
    }

    // Phương thức chuyển đổi DTO sang Entity
    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        if (dto.getProductId() != null) {
            product.setProductId(dto.getProductId());
        }
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setDiscount(dto.getDiscount());
        product.setRealPrice(dto.getRealPrice());
        product.setAuthor(dto.getAuthor());
        product.setPublisher(dto.getPublisher());
        product.setPublicationYear(dto.getPublicationYear());
        product.setPageCount(dto.getPageCount());
        product.setISBN(dto.getISBN());
        product.setSoldCount(dto.getSoldCount());
        product.setActive(true);  // Mặc định là active khi tạo mới
        
        // Chuyển đổi danh sách categories
        if (dto.getCategoryIds() != null) {
            List<Category> categories = new ArrayList<>();
            for (Long categoryId : dto.getCategoryIds()) {
                categoryRepository.findById(categoryId).ifPresent(categories::add);
            }
            product.setCategories(categories);
        }
        
        return product;
    }
}