package com.example.bookstore.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            Integer minRealPrice,
            Integer maxRealPrice,
            Integer year, 
            Long categoryId,
            Integer minStock,
            Integer minSold,
            Double minRating,
            String sortBy,
            String sortDir,
            Pageable pageable) {
        
        System.out.println("===== DEBUGGING SORT PARAMS =====");
        System.out.println("Original sortBy: " + sortBy);
        System.out.println("Original sortDir: " + sortDir);
        System.out.println("Original pageable: " + pageable);
        
        // Lấy thông tin phân trang
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        
        // Xác định hướng sắp xếp
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        
        System.out.println("Direction: " + direction);
        
        // Xác định trường sắp xếp
        String dbField = "product_id"; // mặc định
        
        // Xác định trường sắp xếp theo tham số sortBy
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy) {
                case "name":
                    dbField = "name";
                    break;
                case "price":
                    dbField = "real_price";
                    break;
                case "newest":
                    dbField = "product_id";
                    direction = Sort.Direction.DESC;
                    break;
                case "bestselling":
                case "bestseller":
                    dbField = "sold_count";
                    direction = Sort.Direction.DESC;
                    break;
                case "rating":
                    // Không thể sắp xếp theo rating trong JPA vì nó được tính động
                    // dùng mặc định
                    break;
                case "soldCount":
                    dbField = "sold_count";
                    break;
                default:
                    // Kiểm tra xem có phải là tên trường entity không
                    if (!sortBy.contains("_")) {
                        dbField = convertToSnakeCase(sortBy); // Chuyển từ camelCase sang snake_case
                    } else {
                        dbField = sortBy; // Giữ nguyên snake_case
                    }
            }
        }
        
        System.out.println("Final dbField: " + dbField);
        System.out.println("Final direction: " + direction);
        
        // Tạo đối tượng PageRequest mới với thông tin sắp xếp
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, direction, dbField);
        
        System.out.println("Generated pageRequest: " + pageRequest);
        System.out.println("===== END DEBUGGING =====");
        
        // Gọi repo với pageable mới
        return productRepository.searchProducts(
                name, author, publisher, minPrice, maxPrice, minRealPrice, maxRealPrice,
                year, categoryId, minStock, minSold, minRating, pageRequest)
                .map(this::convertToDTO);
    }

    // Phương thức hỗ trợ chuyển đổi từ camelCase sang snake_case
    private String convertToSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // Xử lý một số trường hợp đặc biệt
        switch (input) {
            case "productId":
                return "product_id";
            case "realPrice":
                return "real_price";
            case "stockQuantity":
                return "stock_quantity";
            case "soldCount":
                return "sold_count";
            case "publicationYear":
                return "publication_year";
        }
        
        // Chuyển đổi chung
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('_');
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
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

    // Phương thức lấy URL ảnh đầu tiên của sản phẩm
    public String getFirstProductImage(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getProductImages() == null || product.getProductImages().isEmpty()) {
            return null;
        }
        
        return product.getProductImages().stream()
                .findFirst()
                .map(ProductImage::getImageURL)
                .orElse(null);
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

    // Lấy các sản phẩm có tồn kho thấp
    public List<ProductDTO> getLowInventoryProducts(int threshold) {
        List<Product> products = productRepository.findAll();
        return products.stream()
            .filter(p -> p.getInventoryCount() < threshold && p.isActive())
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
}