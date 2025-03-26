package com.example.bookstore.dto.statistics;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class RevenueStatisticsDTO {
    private Date date;
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private BigDecimal averageOrderValue;
} 