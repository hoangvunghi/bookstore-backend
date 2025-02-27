package com.example.bookstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.bookstore.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
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
    
    // Tìm kiếm nâng cao kết hợp nhiều tiêu chí
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN p.categories c " +
           "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:author IS NULL OR LOWER(p.author) LIKE LOWER(CONCAT('%', :author, '%'))) " +
           "AND (:publisher IS NULL OR LOWER(p.publisher) LIKE LOWER(CONCAT('%', :publisher, '%'))) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:year IS NULL OR p.publicationYear = :year) " +
           "AND (:categoryId IS NULL OR c.categoryId = :categoryId)")
    Page<Product> searchProducts(
            @Param("name") String name,
            @Param("author") String author,
            @Param("publisher") String publisher,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("year") Integer year,
            @Param("categoryId") Long categoryId,
            Pageable pageable);
            
    // Lấy sách có giảm giá
    Page<Product> findByDiscountGreaterThan(int minDiscount, Pageable pageable);
    
    // Lấy sách mới (theo năm xuất bản)
    Page<Product> findByPublicationYearOrderByPublicationYearDesc(int year, Pageable pageable);
    
    // Lấy sách theo tồn kho
    Page<Product> findByStockQuantityGreaterThan(int minStock, Pageable pageable);
}