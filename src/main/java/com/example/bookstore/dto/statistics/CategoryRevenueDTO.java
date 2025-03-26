package com.example.bookstore.dto.statistics;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CategoryRevenueDTO {
    private Long categoryId;
    private String categoryName;
    private Long totalProducts;
    private Long totalSold;
    private BigDecimal totalRevenue;
    private Double percentage;
} 