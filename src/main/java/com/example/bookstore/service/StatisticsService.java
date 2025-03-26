package com.example.bookstore.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.dto.statistics.CategoryRevenueDTO;
import com.example.bookstore.dto.statistics.OrderStatusStatisticsDTO;
import com.example.bookstore.dto.statistics.RevenueStatisticsDTO;
import com.example.bookstore.dto.statistics.TopProductDTO;
import com.example.bookstore.dto.statistics.UserRegistrationDTO;
import com.example.bookstore.model.Order;
import com.example.bookstore.model.OrderDetail;
import com.example.bookstore.model.Product;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.ProductRepository;
import com.example.bookstore.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // Thống kê doanh thu theo thời gian
    @Transactional(readOnly = true)
    public List<RevenueStatisticsDTO> getRevenueStatistics(Date startDate, Date endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        Map<Date, List<Order>> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(order -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(order.getOrderDate());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    return cal.getTime();
                }));

        List<RevenueStatisticsDTO> statistics = new ArrayList<>();
        ordersByDate.forEach((date, orderList) -> {
            RevenueStatisticsDTO dto = new RevenueStatisticsDTO();
            dto.setDate(date);
            dto.setTotalOrders((long) orderList.size());
            
            BigDecimal totalRevenue = orderList.stream()
                    .map(order -> new BigDecimal(order.getTotalAmount()))
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b, MathContext.DECIMAL128));
            dto.setTotalRevenue(totalRevenue);
            
            if (orderList.size() > 0) {
                dto.setAverageOrderValue(totalRevenue.divide(new BigDecimal(orderList.size()), 2, RoundingMode.HALF_UP));
            }
            
            statistics.add(dto);
        });

        return statistics;
    }

    // Thống kê sản phẩm bán chạy
    @Transactional(readOnly = true)
    public List<TopProductDTO> getTopProducts(int limit, Date startDate, Date endDate) {
        List<Order> paidOrders = orderRepository.findByStatus("PAID");
        List<Order> filteredOrders = paidOrders.stream()
                .filter(order -> order.getOrderDate().compareTo(startDate) >= 0 
                        && order.getOrderDate().compareTo(endDate) <= 0)
                .collect(Collectors.toList());
                
        List<OrderDetail> orderDetails = filteredOrders.stream()
                .flatMap(order -> order.getOrderDetails().stream())
                .collect(Collectors.toList());

        Map<Product, Long> productSales = orderDetails.stream()
                .collect(Collectors.groupingBy(
                        OrderDetail::getProduct,
                        Collectors.summingLong(OrderDetail::getQuantity)));

        Map<Product, BigDecimal> productRevenue = orderDetails.stream()
                .collect(Collectors.groupingBy(
                        OrderDetail::getProduct,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                detail -> new BigDecimal(detail.getPrice()).multiply(new BigDecimal(detail.getQuantity())),
                                (a, b) -> a.add(b, MathContext.DECIMAL128))));

        return productSales.entrySet().stream()
                .map(entry -> {
                    TopProductDTO dto = new TopProductDTO();
                    Product product = entry.getKey();
                    dto.setProductId(product.getProductId());
                    dto.setProductName(product.getName());
                    dto.setTotalSold(entry.getValue());
                    dto.setTotalRevenue(productRevenue.get(product));
                    dto.setImageUrl(product.getProductImages().isEmpty() ? null : product.getProductImages().get(0).getImageURL());
                    return dto;
                })
                .sorted((a, b) -> b.getTotalSold().compareTo(a.getTotalSold()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Thống kê đơn hàng theo trạng thái
    @Transactional(readOnly = true)
    public List<OrderStatusStatisticsDTO> getOrderStatusStatistics(Date startDate, Date endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        long totalOrders = orders.size();
        
        Map<String, List<Order>> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus));

        return ordersByStatus.entrySet().stream()
                .map(entry -> {
                    OrderStatusStatisticsDTO dto = new OrderStatusStatisticsDTO();
                    dto.setStatus(entry.getKey());
                    dto.setCount((long) entry.getValue().size());
                    
                    BigDecimal totalAmount = entry.getValue().stream()
                            .map(order -> new BigDecimal(order.getTotalAmount()))
                            .reduce(BigDecimal.ZERO, (a, b) -> a.add(b, MathContext.DECIMAL128));
                    dto.setTotalAmount(totalAmount);
                    
                    dto.setPercentage((double) entry.getValue().size() / totalOrders * 100);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Thống kê doanh thu theo danh mục
    @Transactional(readOnly = true)
    public List<CategoryRevenueDTO> getCategoryRevenueStatistics(Date startDate, Date endDate) {
        List<Order> paidOrders = orderRepository.findByStatus("PAID");
        List<Order> filteredOrders = paidOrders.stream()
                .filter(order -> order.getOrderDate().compareTo(startDate) >= 0 
                        && order.getOrderDate().compareTo(endDate) <= 0)
                .collect(Collectors.toList());
                
        List<OrderDetail> orderDetails = filteredOrders.stream()
                .flatMap(order -> order.getOrderDetails().stream())
                .collect(Collectors.toList());

        Map<Long, List<OrderDetail>> detailsByCategory = orderDetails.stream()
                .collect(Collectors.groupingBy(detail -> 
                    detail.getProduct().getCategories().iterator().next().getCategoryId()));

        List<CategoryRevenueDTO> statistics = new ArrayList<>();
        detailsByCategory.forEach((categoryId, details) -> {
            CategoryRevenueDTO dto = new CategoryRevenueDTO();
            dto.setCategoryId(categoryId);
            dto.setCategoryName(details.get(0).getProduct().getCategories().iterator().next().getName());
            
            dto.setTotalProducts(productRepository.countByCategoriesCategoryId(categoryId));
            
            long totalSold = details.stream()
                    .mapToLong(OrderDetail::getQuantity)
                    .sum();
            dto.setTotalSold(totalSold);
            
            BigDecimal totalRevenue = details.stream()
                    .map(detail -> new BigDecimal(detail.getPrice()).multiply(new BigDecimal(detail.getQuantity())))
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b, MathContext.DECIMAL128));
            dto.setTotalRevenue(totalRevenue);
            
            BigDecimal totalAllRevenue = orderDetails.stream()
                    .map(detail -> new BigDecimal(detail.getPrice()).multiply(new BigDecimal(detail.getQuantity())))
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b, MathContext.DECIMAL128));
            
            if (totalAllRevenue.compareTo(BigDecimal.ZERO) > 0) {
                dto.setPercentage(totalRevenue.divide(totalAllRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)).doubleValue());
            }
            
            statistics.add(dto);
        });

        return statistics;
    }

    // Thống kê đăng ký người dùng
    @Transactional(readOnly = true)
    public List<UserRegistrationDTO> getUserRegistrationStatistics(Date startDate, Date endDate) {
        List<User> users = userRepository.findByRegistrationDateBetween(startDate, endDate);
        
        Map<Date, List<User>> usersByDate = users.stream()
                .collect(Collectors.groupingBy(user -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(user.getRegistrationDate());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    return cal.getTime();
                }));

        return usersByDate.entrySet().stream()
                .map(entry -> {
                    UserRegistrationDTO dto = new UserRegistrationDTO();
                    dto.setDate(entry.getKey());
                    dto.setTotalRegistrations((long) entry.getValue().size());
                    
                    long activeUsers = entry.getValue().stream()
                            .filter(User::isActive)
                            .count();
                    dto.setActiveUsers(activeUsers);
                    dto.setInactiveUsers(entry.getValue().size() - activeUsers);
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
} 