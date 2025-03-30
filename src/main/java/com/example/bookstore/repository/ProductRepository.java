package com.example.bookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.bookstore.model.Category;
import com.example.bookstore.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Tìm kiếm cơ bản
    Page<Product> findByIsActiveTrue(Pageable pageable);
    Optional<Product> findByProductIdAndIsActiveTrue(Long id);
    Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);
    Page<Product> findByAuthorContainingIgnoreCaseAndIsActiveTrue(String author, Pageable pageable);
    Page<Product> findByPublisherContainingIgnoreCaseAndIsActiveTrue(String publisher, Pageable pageable);
    
    // Tìm kiếm theo tên sách (không phân biệt hoa thường)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Tìm kiếm theo tác giả
    Page<Product> findByAuthorContainingIgnoreCase(String author, Pageable pageable);
    
    // Tìm kiếm theo nhà xuất bản
    Page<Product> findByPublisherContainingIgnoreCase(String publisher, Pageable pageable);
    
    // Tìm kiếm theo ISBN
    Product findByISBN(String isbn);
    
    // Tìm kiếm sách theo khoảng giá
    Page<Product> findByPriceBetween(int minPrice, int maxPrice, Pageable pageable);
    
    // Tìm kiếm sách theo năm xuất bản
    Page<Product> findByPublicationYear(int year, Pageable pageable);
    
    // Tìm kiếm sách theo danh mục
    @Query("SELECT DISTINCT p FROM Product p JOIN p.categories c WHERE c.categoryId = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    // Tìm kiếm nâng cao
    @Query(value = "SELECT DISTINCT p.* FROM products p " +
           "LEFT JOIN productcategory pc ON p.product_id = pc.product_id " +
           "WHERE p.is_active = true " +
           "AND (:name IS NULL OR LOWER(p.name::text) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:author IS NULL OR LOWER(p.author::text) LIKE LOWER(CONCAT('%', :author, '%'))) " +
           "AND (:publisher IS NULL OR LOWER(p.publisher::text) LIKE LOWER(CONCAT('%', :publisher, '%'))) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:minRealPrice IS NULL OR p.real_price >= :minRealPrice) " +
           "AND (:maxRealPrice IS NULL OR p.real_price <= :maxRealPrice) " +
           "AND (:year IS NULL OR p.publication_year = :year) " +
           "AND (:categoryId IS NULL OR pc.category_id = :categoryId) " +
           "AND (:minStock IS NULL OR p.stock_quantity >= :minStock) " +
           "AND (:minSold IS NULL OR p.sold_count >= :minSold) " +
           "AND (:minRating IS NULL OR (SELECT AVG(r.rating) FROM reviews r WHERE r.product_id = p.product_id) >= :minRating)",
           nativeQuery = true)
    Page<Product> searchProducts(
            @Param("name") String name,
            @Param("author") String author,
            @Param("publisher") String publisher,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("minRealPrice") Integer minRealPrice,
            @Param("maxRealPrice") Integer maxRealPrice,
            @Param("year") Integer year,
            @Param("categoryId") Long categoryId,
            @Param("minStock") Integer minStock,
            @Param("minSold") Integer minSold,
            @Param("minRating") Double minRating,
            Pageable pageable);
    
    // Lấy sách có giảm giá
    Page<Product> findByDiscountGreaterThan(int minDiscount, Pageable pageable);
    
    // Lấy sách mới (theo năm xuất bản)
    Page<Product> findByPublicationYearOrderByPublicationYearDesc(int year, Pageable pageable);
    
    // Lấy sách theo tồn kho
    Page<Product> findByStockQuantityGreaterThan(int minStock, Pageable pageable);
    
    // Lấy sách bán chạy (theo số lượng đã bán)
    Page<Product> findBySoldCountGreaterThanOrderBySoldCountDesc(int minSold, Pageable pageable);
    
    // Top selling products
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.soldCount DESC")
    Page<Product> findTopSellingProducts(Pageable pageable);

    List<Product> findByCategories(Category category);
    List<Product> findByCategoriesCategoryId(Long categoryId);
    long countByCategoriesCategoryId(Long categoryId);
}