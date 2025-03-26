package com.example.bookstore.controller;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.statistics.CategoryRevenueDTO;
import com.example.bookstore.dto.statistics.OrderStatusStatisticsDTO;
import com.example.bookstore.dto.statistics.RevenueStatisticsDTO;
import com.example.bookstore.dto.statistics.TopProductDTO;
import com.example.bookstore.dto.statistics.UserRegistrationDTO;
import com.example.bookstore.service.StatisticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "API thống kê")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thống kê doanh thu theo thời gian")
    public ResponseEntity<List<RevenueStatisticsDTO>> getRevenueStatistics(
            @Parameter(description = "Ngày bắt đầu") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Ngày kết thúc") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return ResponseEntity.ok(statisticsService.getRevenueStatistics(startDate, endDate));
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thống kê sản phẩm bán chạy")
    public ResponseEntity<List<TopProductDTO>> getTopProducts(
            @Parameter(description = "Số lượng sản phẩm cần lấy") 
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Ngày bắt đầu") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Ngày kết thúc") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return ResponseEntity.ok(statisticsService.getTopProducts(limit, startDate, endDate));
    }

    @GetMapping("/order-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thống kê đơn hàng theo trạng thái")
    public ResponseEntity<List<OrderStatusStatisticsDTO>> getOrderStatusStatistics(
            @Parameter(description = "Ngày bắt đầu") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Ngày kết thúc") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return ResponseEntity.ok(statisticsService.getOrderStatusStatistics(startDate, endDate));
    }

    @GetMapping("/category-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thống kê doanh thu theo danh mục")
    public ResponseEntity<List<CategoryRevenueDTO>> getCategoryRevenueStatistics(
            @Parameter(description = "Ngày bắt đầu") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Ngày kết thúc") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return ResponseEntity.ok(statisticsService.getCategoryRevenueStatistics(startDate, endDate));
    }

    @GetMapping("/user-registrations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thống kê đăng ký người dùng")
    public ResponseEntity<List<UserRegistrationDTO>> getUserRegistrationStatistics(
            @Parameter(description = "Ngày bắt đầu") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Ngày kết thúc") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return ResponseEntity.ok(statisticsService.getUserRegistrationStatistics(startDate, endDate));
    }
} 