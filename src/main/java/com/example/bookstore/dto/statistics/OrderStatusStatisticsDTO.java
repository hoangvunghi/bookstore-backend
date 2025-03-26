package com.example.bookstore.dto.statistics;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderStatusStatisticsDTO {
    private String status;
    private Long count;
    private BigDecimal totalAmount;
    private Double percentage;
} 