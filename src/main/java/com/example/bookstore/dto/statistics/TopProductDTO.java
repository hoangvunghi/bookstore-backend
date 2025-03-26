package com.example.bookstore.dto.statistics;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TopProductDTO {
    private Long productId;
    private String productName;
    private Long totalSold;
    private BigDecimal totalRevenue;
    private String imageUrl;
} 