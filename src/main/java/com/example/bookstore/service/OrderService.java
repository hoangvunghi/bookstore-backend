package com.example.bookstore.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.dto.OrderDTO;
import com.example.bookstore.dto.OrderDetailDTO;
import com.example.bookstore.model.Order;
import com.example.bookstore.model.OrderDetail;
import com.example.bookstore.model.Product;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.OrderDetailRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.ProductRepository;
import com.example.bookstore.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, 
                       OrderDetailRepository orderDetailRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // Chuyển đổi Order thành OrderDTO
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUser().getUserId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        
        List<OrderDetailDTO> detailDTOs = order.getOrderDetails().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        dto.setOrderDetails(detailDTOs);
        
        return dto;
    }

    // Chuyển đổi OrderDetail thành OrderDetailDTO
    private OrderDetailDTO convertToDTO(OrderDetail detail) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setOrderDetailId(detail.getOrderDetailId());
        dto.setOrderId(detail.getOrder().getOrderId());
        dto.setProductId(detail.getProduct().getProductId());
        dto.setQuantity(detail.getQuantity());
        dto.setPrice(detail.getPrice());
        return dto;
    }

    // Lấy tất cả đơn hàng của một người dùng
    public Page<OrderDTO> getOrdersByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Page.empty();
        }
        return orderRepository.findByUser(user, pageable)
                .map(this::convertToDTO);
    }

    // Lấy chi tiết một đơn hàng
    public OrderDTO getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    // Tạo đơn hàng mới
    @Transactional
    public OrderDTO createOrder(Long userId, List<OrderDetailDTO> items) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || items.isEmpty()) {
            return null;
        }

        // Tạo đơn hàng mới
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus("PENDING");
        order = orderRepository.save(order);

        // Tạo chi tiết đơn hàng
        List<OrderDetail> details = new ArrayList<>();
        int totalAmount = 0;

        for (OrderDetailDTO item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product == null || item.getQuantity() <= 0) {
                continue;
            }

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setPrice(product.getPrice());
            
            details.add(detail);
            totalAmount += detail.getPrice() * detail.getQuantity();
        }

        // Lưu chi tiết đơn hàng
        orderDetailRepository.saveAll(details);

        // Cập nhật tổng tiền
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        return convertToDTO(order);
    }

    // Cập nhật trạng thái đơn hàng
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return null;
        }

        order.setStatus(status);
        order = orderRepository.save(order);
        return convertToDTO(order);
    }

    // Hủy đơn hàng
    @Transactional
    public boolean cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getStatus().equals("PENDING")) {
            return false;
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
        return true;
    }

    // Lấy danh sách đơn hàng theo trạng thái
    public Page<OrderDTO> getOrdersByStatus(String status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }
} 